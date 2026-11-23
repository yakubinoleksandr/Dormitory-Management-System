package ua.kpi.dormitory.cabinet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.inventory.Room;
import ua.kpi.dormitory.payment.PaymentRepository;
import ua.kpi.dormitory.profile.ResidentProfile;
import ua.kpi.dormitory.profile.ResidentProfileRepository;
import ua.kpi.dormitory.security.AuthenticatedUser;
import ua.kpi.dormitory.settlement.Settlement;
import ua.kpi.dormitory.settlement.SettlementRepository;
import ua.kpi.dormitory.settlement.SettlementStatus;

import java.math.BigDecimal;
import java.util.List;

/** FR-10: особистий кабінет. Повертає лише дані самого мешканця; сусіди — тільки ПІБ (NFR-04). */
@Service
public class CabinetService {
    private final ResidentProfileRepository profiles;
    private final SettlementRepository settlements;
    private final PaymentRepository payments;

    public CabinetService(ResidentProfileRepository profiles, SettlementRepository settlements, PaymentRepository payments) {
        this.profiles = profiles;
        this.settlements = settlements;
        this.payments = payments;
    }

    @Transactional(readOnly = true)
    public CabinetDtos.CabinetResponse get(AuthenticatedUser user) {
        ResidentProfile profile = profiles.findByAccountId(user.id())
                .orElseThrow(() -> ApiException.notFound("Профіль не знайдено"));
        Settlement s = settlements.findByResidentAccountIdAndStatus(user.id(), SettlementStatus.ACTIVE).orElse(null);
        if (s == null) {
            return new CabinetDtos.CabinetResponse(profile.getFullName(), profile.getCourse(), profile.getGroupName(),
                    null, BigDecimal.ZERO, List.of());
        }
        Room room = s.getBed().getRoom();
        Long dormitoryId = room.getDormitory().getId();
        List<String> neighbours = room.getBlockName() != null
                ? settlements.neighbourNamesInBlock(dormitoryId, room.getBlockName(), user.id())
                : settlements.neighbourNamesInRoom(room.getId(), user.id());
        var placement = new CabinetDtos.Placement(s.getId(), s.getContractNumber(), s.getStartDate(), s.getEndDate(),
                dormitoryId, room.getDormitory().getNameOrNumber(), room.getFloor(), room.getBlockName(),
                room.getRoomNumber(), room.getId(), s.getBed().getBedNumber());
        return new CabinetDtos.CabinetResponse(profile.getFullName(), profile.getCourse(), profile.getGroupName(),
                placement, payments.sumVerified(s.getId()), neighbours);
    }
}
