package ua.kpi.dormitory.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.account.Account;
import ua.kpi.dormitory.account.AccountRepository;
import ua.kpi.dormitory.account.Role;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.inventory.Dormitory;
import ua.kpi.dormitory.inventory.DormitoryRepository;

import java.time.LocalDateTime;

/** FR-03: призначення завідуючої до гуртожитку. */
@Service
public class ManagerAssignmentService {
    private final DormitoryManagerRepository assignments;
    private final AccountRepository accounts;
    private final DormitoryRepository dormitories;

    public ManagerAssignmentService(DormitoryManagerRepository assignments, AccountRepository accounts,
                                    DormitoryRepository dormitories) {
        this.assignments = assignments;
        this.accounts = accounts;
        this.dormitories = dormitories;
    }

    @Transactional
    public void assign(Long managerAccountId, Long dormitoryId) {
        Account manager = accounts.findById(managerAccountId)
                .orElseThrow(() -> ApiException.notFound("Акаунт не знайдено"));
        if (!Role.MANAGER.equals(manager.getRole().getName())) {
            throw ApiException.badRequest("Акаунт не має ролі " + Role.MANAGER);
        }
        Dormitory dormitory = dormitories.findById(dormitoryId)
                .orElseThrow(() -> ApiException.notFound("Гуртожиток не знайдено"));
        if (assignments.existsByAccountIdAndDormitoryId(managerAccountId, dormitoryId)) {
            throw ApiException.conflict("Завідуюча вже призначена до цього гуртожитку");
        }
        DormitoryManager link = new DormitoryManager();
        link.setAccount(manager);
        link.setDormitory(dormitory);
        link.setAssignedAt(LocalDateTime.now());
        assignments.save(link);
    }
}
