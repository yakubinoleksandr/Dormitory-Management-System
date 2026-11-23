package ua.kpi.dormitory.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Наскрізний сценарій інтеграції модулів на реальній PostgreSQL 15 з міграціями Flyway
 * (ddl-auto=validate перевіряє відповідність JPA-моделі схемі). Запуск: mvn verify (потрібен Docker).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SettlementFlowIT {

    static {
        // страховка для запуску з IDE, де argLine з pom.xml не застосовується
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("app.admin.username", () -> "admin");
        r.add("app.admin.password", () -> "Admin-pass-123");
        r.add("app.jwt.secret", () -> "integration-test-secret-integration-test-secret");
        r.add("app.storage.receipts-dir", () -> Path.of(System.getProperty("java.io.tmpdir"), "dms-it-receipts").toString());
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    // стан між кроками (кроки виконуються за порядком @Order)
    static String admin, manager, res1, res2;
    static long dormId, room1, room2, bedA, bedB, bedC, appId1, appId2, settlement1, settlement2, resident2Id, repairId;

    // ---------- допоміжні методи ----------
    private JsonNode call(MockHttpServletRequestBuilder req, String token, Object body, int expected) throws Exception {
        if (token != null) req.header("Authorization", "Bearer " + token);
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        MvcResult result = mvc.perform(req).andExpect(status().is(expected)).andReturn();
        String content = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return content.isBlank() ? json.createObjectNode() : json.readTree(content);
    }

    private String login(String user, String pass) throws Exception {
        return call(post("/api/auth/login"), null, java.util.Map.of("username", user, "password", pass), 200).get("token").asText();
    }

    private JsonNode roomOf(long roomId) throws Exception {
        for (JsonNode r : call(get("/api/dormitories/" + dormId + "/rooms"), manager, null, 200)) {
            if (r.get("id").asLong() == roomId) return r;
        }
        throw new AssertionError("кімнату не знайдено на карті");
    }

    // ---------- сценарій ----------
    @Test @Order(1)
    void adminConfiguresDormitoryAndAccounts() throws Exception {
        admin = login("admin", "Admin-pass-123");
        dormId = call(post("/api/dormitories"), admin, java.util.Map.of("nameOrNumber", "ГУР-5", "address", "Київ",
                "totalFloors", 5, "layoutType", "BLOCK"), 201).get("id").asLong();

        // 2 поверхи × 1 блок × 2 кімнати, по 2 місця => 4 кімнати
        JsonNode gen = call(post("/api/dormitories/" + dormId + "/rooms/generate"), admin,
                java.util.Map.of("floors", 2, "blocksPerFloor", 1, "roomsPerUnit", 2, "capacity", 2), 201);
        assertEquals(4, gen.get("roomsCreated").asInt());
        // повторний запуск не дублює структуру
        call(post("/api/dormitories/" + dormId + "/rooms/generate"), admin,
                java.util.Map.of("floors", 2, "blocksPerFloor", 1, "roomsPerUnit", 2, "capacity", 2), 409);

        long managerId = call(post("/api/accounts"), admin, java.util.Map.of("username", "manager1", "email", "m1@test.ua",
                "password", "Manager-pass-1", "role", "ROLE_MANAGER"), 201).get("id").asLong();
        call(post("/api/dormitories/" + dormId + "/managers"), admin, java.util.Map.of("managerAccountId", managerId), 201);

        call(post("/api/accounts"), admin, java.util.Map.of("username", "res1", "email", "r1@test.ua", "password", "Resident-pass-1",
                "role", "ROLE_RESIDENT", "fullName", "Іваненко Іван"), 201);
        resident2Id = call(post("/api/accounts"), admin, java.util.Map.of("username", "res2", "email", "r2@test.ua", "password", "Resident-pass-2",
                "role", "ROLE_RESIDENT", "fullName", "Петренко Петро"), 201).get("id").asLong();
        // мешканець без ПІБ відхиляється
        call(post("/api/accounts"), admin, java.util.Map.of("username", "res3", "email", "r3@test.ua", "password", "Resident-pass-3",
                "role", "ROLE_RESIDENT"), 400);
    }

    @Test @Order(2)
    void residentsApplyAndManagerSeesRoomMap() throws Exception {
        manager = login("manager1", "Manager-pass-1");
        res1 = login("res1", "Resident-pass-1");
        res2 = login("res2", "Resident-pass-2");

        appId1 = call(post("/api/settlement-applications"), res1, java.util.Map.of("dormitoryId", dormId), 201).get("id").asLong();
        appId2 = call(post("/api/settlement-applications"), res2, java.util.Map.of("dormitoryId", dormId), 201).get("id").asLong();
        // дублікат активної заявки
        call(post("/api/settlement-applications"), res1, java.util.Map.of("dormitoryId", dormId), 409);

        JsonNode rooms = call(get("/api/dormitories/" + dormId + "/rooms"), manager, null, 200);
        assertEquals(4, rooms.size());
        assertEquals("FREE", rooms.get(0).get("status").asText());
        assertEquals("101/1", rooms.get(0).get("roomNumber").asText());
        room1 = rooms.get(0).get("id").asLong();
        room2 = rooms.get(1).get("id").asLong();

        JsonNode beds1 = call(get("/api/dormitories/" + dormId + "/rooms/" + room1 + "/beds"), manager, null, 200);
        bedA = beds1.get(0).get("id").asLong();
        bedB = beds1.get(1).get("id").asLong();
        bedC = call(get("/api/dormitories/" + dormId + "/rooms/" + room2 + "/beds"), manager, null, 200).get(0).get("id").asLong();
    }

    @Test @Order(3)
    void settlementIsAtomicAndRejectsConflicts() throws Exception {
        var body1 = java.util.Map.of("applicationId", appId1, "bedId", bedA, "contractNumber", "К-001",
                "startDate", "2026-09-01", "endDate", "2027-06-30");
        settlement1 = call(post("/api/settlements"), manager, body1, 201).get("id").asLong();
        assertEquals("PARTIAL", roomOf(room1).get("status").asText());

        // те саме місце для другої заяви => 409, договір не створюється, заява залишається PENDING
        call(post("/api/settlements"), manager, java.util.Map.of("applicationId", appId2, "bedId", bedA,
                "contractNumber", "К-002", "startDate", "2026-09-01", "endDate", "2027-06-30"), 409);
        // повторне використання вже схваленої заяви => 409
        call(post("/api/settlements"), manager, body1, 409);

        settlement2 = call(post("/api/settlements"), manager, java.util.Map.of("applicationId", appId2, "bedId", bedB,
                "contractNumber", "К-002", "startDate", "2026-09-01", "endDate", "2027-06-30"), 201).get("id").asLong();
        assertEquals("FULL", roomOf(room1).get("status").asText());
        assertEquals(0, roomOf(room1).get("freeBeds").asInt());
    }

    @Test @Order(4)
    void residentCabinetShowsOnlyOwnDataAndNeighbourNames() throws Exception {
        JsonNode cab = call(get("/api/me/cabinet"), res1, null, 200);
        assertEquals("К-001", cab.get("placement").get("contractNumber").asText());
        assertEquals("101/1", cab.get("placement").get("roomNumber").asText());
        assertEquals(1, cab.get("neighbours").size());
        assertEquals("Петренко Петро", cab.get("neighbours").get(0).asText());
        assertTrue(cab.get("neighbours").get(0).isTextual(), "сусіди повертаються лише як ПІБ");
    }

    @Test @Order(5)
    void accessControlByRoleAndOwnership() throws Exception {
        mvc.perform(get("/api/dormitories")).andExpect(status().isUnauthorized());
        call(get("/api/reports/residents?dormitoryId=" + dormId), res1, null, 403);
        call(get("/api/settlement-applications?dormitoryId=" + dormId), res1, null, 403);
        call(post("/api/dormitories"), manager, java.util.Map.of("nameOrNumber", "X", "totalFloors", 1, "layoutType", "CORRIDOR"), 403);
        call(get("/api/search?q=Іван"), res1, null, 403);
        // вихід деактивує сесію
        String temp = login("res1", "Resident-pass-1");
        call(post("/api/auth/logout"), temp, null, 204);
        call(get("/api/me/cabinet"), temp, null, 401);
    }

    @Test @Order(6)
    void searchAndReports() throws Exception {
        JsonNode found = call(get("/api/search?q=петренко"), manager, null, 200);
        assertEquals(1, found.get("residents").size());
        assertEquals(1, call(get("/api/search?q=К-001"), manager, null, 200).get("residents").size());
        assertEquals(2, call(get("/api/reports/residents?dormitoryId=" + dormId), manager, null, 200).size());

        byte[] xlsx = mvc.perform(get("/api/reports/residents/export?dormitoryId=" + dormId + "&format=xlsx")
                .header("Authorization", "Bearer " + manager)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertEquals('P', xlsx[0]);   // ZIP-контейнер OOXML починається з "PK"
        assertEquals('K', xlsx[1]);

        byte[] pdf = mvc.perform(get("/api/reports/free-beds/export?dormitoryId=" + dormId + "&format=pdf")
                .header("Authorization", "Bearer " + manager)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test @Order(7)
    void repairLifecycleFollowsStateDiagram() throws Exception {
        // мешканець не може подати заявку на чужу кімнату
        call(post("/api/repair-requests"), res2, java.util.Map.of("roomId", room2, "category", "PLUMBING", "description", "Протікає кран"), 403);

        repairId = call(post("/api/repair-requests"), res2, java.util.Map.of("roomId", room1, "category", "PLUMBING",
                "description", "Протікає кран"), 201).get("id").asLong();
        String path = "/api/repair-requests/" + repairId + "/status";
        call(patch(path), manager, java.util.Map.of("status", "COMPLETED"), 409);          // NEW -> COMPLETED заборонено
        call(patch(path), manager, java.util.Map.of("status", "REJECTED"), 400);           // відхилення без коментаря
        call(patch(path), manager, java.util.Map.of("status", "IN_PROGRESS"), 200);
        call(patch(path), manager, java.util.Map.of("status", "COMPLETED", "managerComment", "Замінено змішувач"), 200);
        call(patch(path), manager, java.util.Map.of("status", "IN_PROGRESS"), 409);        // кінцевий стан
        JsonNode mine = call(get("/api/repair-requests/mine"), res2, null, 200);
        assertEquals("COMPLETED", mine.get(0).get("status").asText());
        assertEquals("Замінено змішувач", mine.get(0).get("managerComment").asText());
    }

    @Test @Order(8)
    void paymentReceiptVerification() throws Exception {
        MockMultipartFile receipt = new MockMultipartFile("receipt", "r.pdf", "application/pdf", "%PDF-1.4 test".getBytes());
        MvcResult created = mvc.perform(multipart("/api/settlements/" + settlement2 + "/payments").file(receipt)
                        .param("amount", "1500.00").param("method", "card").header("Authorization", "Bearer " + res2))
                .andExpect(status().isCreated()).andReturn();
        long paymentId = json.readTree(created.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).get("id").asLong();

        // чужий мешканець не бачить квитанцію
        call(get("/api/payments/" + paymentId + "/receipt"), res1, null, 403);
        mvc.perform(get("/api/payments/" + paymentId + "/receipt").header("Authorization", "Bearer " + manager)).andExpect(status().isOk());

        assertEquals("0", call(get("/api/me/cabinet"), res2, null, 200).get("verifiedPaymentsTotal").asText().replaceAll("\\.0+$", ""));
        call(patch("/api/payments/" + paymentId + "/status"), manager, java.util.Map.of("status", "VERIFIED"), 200);
        assertEquals(0, new java.math.BigDecimal("1500.00")
                .compareTo(new java.math.BigDecimal(call(get("/api/me/cabinet"), res2, null, 200).get("verifiedPaymentsTotal").asText())));
        call(patch("/api/payments/" + paymentId + "/status"), manager, java.util.Map.of("status", "REJECTED"), 409);
    }

    @Test @Order(9)
    void moveBetweenRoomsAndMoveOutRecalculateStatuses() throws Exception {
        // переселення res2 у кімнату 2: стара кімната -> PARTIAL, нова -> PARTIAL, договір і платежі зберігаються
        call(patch("/api/settlements/" + settlement2 + "/bed"), manager, java.util.Map.of("bedId", bedC), 200);
        assertEquals("PARTIAL", roomOf(room1).get("status").asText());
        assertEquals("PARTIAL", roomOf(room2).get("status").asText());
        assertEquals(1, call(get("/api/settlements/" + settlement2 + "/payments"), manager, null, 200).size());
        // зайняте місце
        call(patch("/api/settlements/" + settlement2 + "/bed"), manager, java.util.Map.of("bedId", bedA), 409);

        // виселення res1: кімната 1 -> FREE; повторне виселення не змінює зайнятість
        call(patch("/api/settlements/" + settlement1 + "/move-out"), manager, null, 200);
        assertEquals("FREE", roomOf(room1).get("status").asText());
        call(patch("/api/settlements/" + settlement1 + "/move-out"), manager, null, 409);
        assertEquals("FREE", roomOf(room1).get("status").asText());

        assertTrue(resident2Id > 0);
    }

    @Test @Order(10)
    void profileEditAndAnnouncements() throws Exception {
        call(put("/api/residents/" + resident2Id + "/profile"), manager,
                java.util.Map.of("fullName", "Петренко Петро Петрович", "course", 3, "groupName", "ІО-44", "phone", "+380501112233"), 200);
        assertEquals("ІО-44", call(get("/api/profile/me"), res2, null, 200).get("groupName").asText());

        call(post("/api/announcements"), manager, java.util.Map.of("dormitoryId", dormId, "title", "Збори", "content", "О 18:00"), 201);
        assertEquals(1, call(get("/api/announcements?dormitoryId=" + dormId), res2, null, 200).size());
        // виселений мешканець більше не бачить оголошень корпусу
        call(get("/api/announcements?dormitoryId=" + dormId), res1, null, 403);
    }
}