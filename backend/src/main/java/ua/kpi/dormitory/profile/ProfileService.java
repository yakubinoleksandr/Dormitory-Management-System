package ua.kpi.dormitory.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.AuthenticatedUser;
import ua.kpi.dormitory.settlement.SettlementRepository;

/** FR-09: редагування контактних даних мешканця завідуючою (свого корпусу) або адміністратором. */
@Service
public class ProfileService {
    private final ResidentProfileRepository profiles;
    private final SettlementRepository settlements;
    private final AccessPolicy access;

    public ProfileService(ResidentProfileRepository profiles, SettlementRepository settlements, AccessPolicy access) {
        this.profiles = profiles;
        this.settlements = settlements;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public ProfileDtos.ProfileResponse get(Long accountId) {
        return toResponse(find(accountId));
    }

    @Transactional
    public ProfileDtos.ProfileResponse update(AuthenticatedUser actor, Long accountId, ProfileDtos.UpdateProfileRequest req) {
        if (actor.isManager()) {
            // завідуюча змінює лише мешканців, які мають договір у її гуртожитку
            boolean allowed = settlements.findDormitoryIdsByResident(accountId).stream()
                    .anyMatch(id -> {
                        try {
                            access.requireManagerOf(actor, id);
                            return true;
                        } catch (ApiException e) {
                            return false;
                        }
                    });
            if (!allowed) {
                throw ApiException.forbidden("Мешканець не належить до ваших гуртожитків");
            }
        }
        ResidentProfile p = find(accountId);
        p.setFullName(req.fullName());
        p.setCourse(req.course());
        p.setGroupName(req.groupName());
        p.setPhone(req.phone());
        p.setBenefitsInfo(req.benefitsInfo());
        return toResponse(p);
    }

    private ResidentProfile find(Long accountId) {
        return profiles.findByAccountId(accountId).orElseThrow(() -> ApiException.notFound("Профіль не знайдено"));
    }

    private static ProfileDtos.ProfileResponse toResponse(ResidentProfile p) {
        return new ProfileDtos.ProfileResponse(p.getAccount().getId(), p.getFullName(), p.getCourse(),
                p.getGroupName(), p.getPhone(), p.getBenefitsInfo());
    }
}
