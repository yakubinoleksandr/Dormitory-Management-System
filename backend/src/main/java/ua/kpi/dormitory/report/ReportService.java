package ua.kpi.dormitory.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.AuthenticatedUser;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FR-18: списки проживаючих і вільних місць у JSON та експорт у Excel/PDF.
 * Список боржників не формується: у схемі БД немає тарифів і нарахувань (лаб. 2, п. 10.2).
 */
@Service
public class ReportService {
    private final ReportRepository reports;
    private final AccessPolicy access;
    private final Map<String, ReportExporter> exporters = new HashMap<>();

    public ReportService(ReportRepository reports, AccessPolicy access, List<ReportExporter> exporterBeans) {
        this.reports = reports;
        this.access = access;
        exporterBeans.forEach(e -> exporters.put(e.format(), e));
    }

    @Transactional(readOnly = true)
    public List<ResidentRow> residents(AuthenticatedUser manager, Long dormitoryId) {
        access.requireManagerOf(manager, dormitoryId);
        return reports.activeResidents(dormitoryId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> freeBeds(AuthenticatedUser manager, Long dormitoryId) {
        access.requireManagerOf(manager, dormitoryId);
        return reports.freeBeds(dormitoryId).stream()
                .map(row -> Map.<String, Object>of("roomNumber", row[0], "bedNumber", row[1])).toList();
    }

    public ReportExporter exporter(String format) {
        ReportExporter exporter = exporters.get(format == null ? "" : format.toLowerCase());
        if (exporter == null) {
            throw ApiException.badRequest("Підтримувані формати: " + String.join(", ", exporters.keySet()));
        }
        return exporter;
    }

    /** @param type residents | free-beds */
    @Transactional(readOnly = true)
    public ReportTable table(AuthenticatedUser manager, Long dormitoryId, String type) {
        access.requireManagerOf(manager, dormitoryId);
        String date = LocalDate.now().toString();
        return switch (type) {
            case "residents" -> new ReportTable("Список проживаючих, " + date,
                    List.of("Договір", "ПІБ", "Кімната", "Ліжко"),
                    reports.activeResidents(dormitoryId).stream()
                            .map(r -> List.of(r.contractNumber(), r.fullName(), r.roomNumber(), String.valueOf(r.bedNumber())))
                            .toList());
            case "free-beds" -> new ReportTable("Вільні місця, " + date,
                    List.of("Кімната", "Ліжко"),
                    reports.freeBeds(dormitoryId).stream()
                            .map(r -> List.of(String.valueOf(r[0]), String.valueOf(r[1]))).toList());
            default -> throw ApiException.badRequest("Невідомий тип звіту: " + type + " (residents, free-beds)");
        };
    }
}
