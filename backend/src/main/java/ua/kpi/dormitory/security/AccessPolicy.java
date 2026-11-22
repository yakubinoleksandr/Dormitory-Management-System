package ua.kpi.dormitory.security;

import org.springframework.stereotype.Component;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.profile.DormitoryManagerRepository;
import ua.kpi.dormitory.settlement.SettlementRepository;

/** Централізована перевірка прав на конкретний гуртожиток (NFR-04): роль + приналежність об'єкта. */
@Component
public class AccessPolicy {
    private final DormitoryManagerRepository managers;
    private final SettlementRepository settlements;

    public AccessPolicy(DormitoryManagerRepository managers, SettlementRepository settlements) {
        this.managers = managers;
        this.settlements = settlements;
    }

    /** Лише завідуюча, закріплена за гуртожитком. */
    public void requireManagerOf(AuthenticatedUser user, Long dormitoryId) {
        if (!user.isManager() || !managers.existsByAccountIdAndDormitoryId(user.id(), dormitoryId)) {
            throw ApiException.forbidden("Немає доступу до цього гуртожитку");
        }
    }

    /** Адміністратор; завідуюча свого корпусу; мешканець із активним договором у цьому корпусі. */
    public void requireCanView(AuthenticatedUser user, Long dormitoryId) {
        boolean allowed = user.isAdmin()
                || (user.isManager() && managers.existsByAccountIdAndDormitoryId(user.id(), dormitoryId))
                || (user.isResident() && settlements.existsActiveInDormitory(user.id(), dormitoryId));
        if (!allowed) {
            throw ApiException.forbidden("Немає доступу до цього гуртожитку");
        }
    }
}
