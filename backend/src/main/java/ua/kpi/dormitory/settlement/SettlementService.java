package ua.kpi.dormitory.settlement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.account.AccountRepository;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.inventory.*;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.AuthenticatedUser;

/**
 * Поселення та виселення (FR-07, FR-08). Усі зміни — в одній транзакції; порядок блокувань
 * єдиний: акаунт мешканця → заява/договір → кімната (лаб. 2, п. 6.1).
 */
@Service
public class SettlementService {
    private final AccountRepository accounts;
    private final SettlementApplicationRepository applications;
    private final SettlementRepository settlements;
    private final RoomRepository rooms;
    private final BedRepository beds;
    private final AccessPolicy access;

    public SettlementService(AccountRepository accounts, SettlementApplicationRepository applications,
                             SettlementRepository settlements, RoomRepository rooms, BedRepository beds,
                             AccessPolicy access) {
        this.accounts = accounts;
        this.applications = applications;
        this.settlements = settlements;
        this.rooms = rooms;
        this.beds = beds;
        this.access = access;
    }

    @Transactional
    public SettlementDtos.SettlementResponse settle(AuthenticatedUser manager, SettlementDtos.SettleRequest req) {
        if (req.startDate().isAfter(req.endDate())) {
            throw ApiException.badRequest("start_date не може бути пізнішою за end_date");
        }
        Long residentId = applications.findResidentIdById(req.applicationId())
                .orElseThrow(() -> ApiException.notFound("Заявку не знайдено"));

        accounts.lockById(residentId);                                       // 1. акаунт мешканця
        SettlementApplication app = applications.lockById(req.applicationId()) // 2. заява
                .orElseThrow(() -> ApiException.notFound("Заявку не знайдено"));
        access.requireManagerOf(manager, app.getDormitory().getId());
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw ApiException.conflict("Заява вже розглянута");
        }

        Long roomId = beds.findRoomIdByBedId(req.bedId())
                .orElseThrow(() -> ApiException.notFound("Ліжко-місце не знайдено"));
        Room room = rooms.lockById(roomId)                                   // 3. кімната
                .orElseThrow(() -> ApiException.notFound("Кімнату не знайдено"));
        Bed bed = beds.findById(req.bedId()).orElseThrow();                  // стан місця читається після блокування

        if (!room.getDormitory().getId().equals(app.getDormitory().getId())) {
            throw ApiException.badRequest("Ліжко-місце не належить до гуртожитку заяви");
        }
        if (room.getStatus() == RoomStatus.REPAIR) {
            throw ApiException.conflict("Кімната на ремонті: поселення заборонене");
        }
        if (bed.isOccupied() || settlements.existsByBedIdAndStatus(bed.getId(), SettlementStatus.ACTIVE)) {
            throw ApiException.conflict("Ліжко-місце вже зайняте");
        }
        if (settlements.existsByResidentAccountIdAndStatus(residentId, SettlementStatus.ACTIVE)) {
            throw ApiException.conflict("Мешканець уже має активний договір");
        }
        if (settlements.existsByContractNumber(req.contractNumber())) {
            throw ApiException.conflict("Номер договору вже існує");
        }

        Settlement s = new Settlement();
        s.setApplication(app);
        s.setResidentAccount(app.getResidentAccount());
        s.setBed(bed);
        s.setContractNumber(req.contractNumber());
        s.setStartDate(req.startDate());
        s.setEndDate(req.endDate());
        s.setStatus(SettlementStatus.ACTIVE);
        s = settlements.save(s);

        app.setStatus(ApplicationStatus.APPROVED);   // схвалення і створення договору атомарні
        bed.setOccupied(true);
        refreshRoomStatus(room);
        return toResponse(s);
    }

    @Transactional
    public SettlementDtos.SettlementResponse moveOut(AuthenticatedUser manager, Long settlementId) {
        Long residentId = settlements.findResidentIdById(settlementId)
                .orElseThrow(() -> ApiException.notFound("Договір не знайдено"));
        access.requireManagerOf(manager, settlements.findDormitoryIdById(settlementId).orElseThrow());

        accounts.lockById(residentId);
        Settlement s = settlements.lockById(settlementId)
                .orElseThrow(() -> ApiException.notFound("Договір не знайдено"));
        if (s.getStatus() == SettlementStatus.MOVED_OUT) {
            throw ApiException.conflict("Договір уже закрито");
        }
        Long roomId = settlements.findRoomIdById(settlementId).orElseThrow();
        Room room = rooms.lockById(roomId).orElseThrow();
        Bed bed = beds.findById(s.getBed().getId()).orElseThrow();

        s.setStatus(SettlementStatus.MOVED_OUT);
        bed.setOccupied(false);
        refreshRoomStatus(room);
        return toResponse(s);
    }

    /** Переселення в межах гуртожитку (FR-08): заплановано на наступну ітерацію. */
    public SettlementDtos.SettlementResponse changeBed(AuthenticatedUser manager, Long settlementId, Long newBedId) {
        throw ApiException.notImplemented("Переселення буде реалізовано в наступній ітерації");
    }

    private void refreshRoomStatus(Room room) {
        long occupied = beds.countByRoomIdAndOccupiedTrue(room.getId());
        room.setStatus(RoomStatusCalculator.recalculate(room.getStatus(), room.getCapacity(), occupied));
    }

    private static SettlementDtos.SettlementResponse toResponse(Settlement s) {
        return new SettlementDtos.SettlementResponse(s.getId(), s.getApplication().getId(),
                s.getResidentAccount().getId(), s.getBed().getId(), s.getContractNumber(),
                s.getStartDate(), s.getEndDate(), s.getStatus());
    }
}
