package ua.kpi.dormitory.repair;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.account.AccountRepository;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.inventory.RoomRepository;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.AuthenticatedUser;
import ua.kpi.dormitory.settlement.SettlementRepository;

import java.time.LocalDateTime;
import java.util.List;

/** FR-13–FR-15: створення заявки, обробка завідуючою, перегляд статусу. */
@Service
public class RepairService {
    private final RepairRequestRepository repairs;
    private final RoomRepository rooms;
    private final AccountRepository accounts;
    private final SettlementRepository settlements;
    private final AccessPolicy access;

    public RepairService(RepairRequestRepository repairs, RoomRepository rooms, AccountRepository accounts,
                         SettlementRepository settlements, AccessPolicy access) {
        this.repairs = repairs;
        this.rooms = rooms;
        this.accounts = accounts;
        this.settlements = settlements;
        this.access = access;
    }

    @Transactional
    public RepairDtos.RepairResponse create(AuthenticatedUser resident, RepairDtos.CreateRepairRequest req) {
        if (!settlements.existsActiveInRoom(resident.id(), req.roomId())) {
            throw ApiException.forbidden("Заявку можна подати лише на власну кімнату");
        }
        RepairRequest r = new RepairRequest();
        r.setResidentAccount(accounts.getReferenceById(resident.id()));
        r.setRoom(rooms.getReferenceById(req.roomId()));
        r.setCategory(req.category());
        r.setDescription(req.description());
        r.setStatus(RepairStatus.NEW);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(r.getCreatedAt());
        return toResponse(repairs.save(r));
    }

    @Transactional(readOnly = true)
    public List<RepairDtos.RepairResponse> mine(AuthenticatedUser resident) {
        return repairs.findByResidentAccountIdOrderByCreatedAtDesc(resident.id()).stream().map(RepairService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RepairDtos.RepairResponse> byDormitory(AuthenticatedUser manager, Long dormitoryId) {
        access.requireManagerOf(manager, dormitoryId);
        return repairs.findByRoomDormitoryIdOrderByCreatedAtDesc(dormitoryId).stream().map(RepairService::toResponse).toList();
    }

    @Transactional
    public RepairDtos.RepairResponse changeStatus(AuthenticatedUser manager, Long id, RepairDtos.ChangeStatusRequest req) {
        Long dormitoryId = repairs.findDormitoryIdById(id).orElseThrow(() -> ApiException.notFound("Заявку не знайдено"));
        access.requireManagerOf(manager, dormitoryId);

        RepairRequest r = repairs.lockById(id).orElseThrow(() -> ApiException.notFound("Заявку не знайдено"));
        if (!RepairStatusPolicy.canTransition(r.getStatus(), req.status())) {
            throw ApiException.conflict("Перехід " + r.getStatus() + " → " + req.status() + " не дозволений");
        }
        boolean blankComment = req.managerComment() == null || req.managerComment().isBlank();
        if (RepairStatusPolicy.commentRequired(req.status()) && blankComment) {
            throw ApiException.badRequest("Для відхилення потрібен коментар");
        }
        r.setStatus(req.status());
        if (!blankComment) {
            r.setManagerComment(req.managerComment());
        }
        r.setUpdatedAt(LocalDateTime.now());
        return toResponse(r);
    }

    private static RepairDtos.RepairResponse toResponse(RepairRequest r) {
        return new RepairDtos.RepairResponse(r.getId(), r.getRoom().getId(), r.getCategory(), r.getDescription(),
                r.getStatus(), r.getManagerComment(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
