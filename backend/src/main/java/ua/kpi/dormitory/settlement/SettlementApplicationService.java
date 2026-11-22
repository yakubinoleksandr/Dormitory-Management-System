package ua.kpi.dormitory.settlement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.account.AccountRepository;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.inventory.DormitoryRepository;
import ua.kpi.dormitory.security.AuthenticatedUser;

import java.time.LocalDateTime;
import java.util.List;

/** FR-06: мешканець подає власну заявку на поселення (акаунт береться з токена). */
@Service
public class SettlementApplicationService {
    private final SettlementApplicationRepository applications;
    private final AccountRepository accounts;
    private final DormitoryRepository dormitories;

    public SettlementApplicationService(SettlementApplicationRepository applications, AccountRepository accounts,
                                        DormitoryRepository dormitories) {
        this.applications = applications;
        this.accounts = accounts;
        this.dormitories = dormitories;
    }

    @Transactional
    public SettlementDtos.ApplicationResponse create(AuthenticatedUser user, Long dormitoryId) {
        if (applications.existsByResidentAccountIdAndDormitoryIdAndStatus(user.id(), dormitoryId, ApplicationStatus.PENDING)) {
            throw ApiException.conflict("Заявка до цього гуртожитку вже очікує розгляду");
        }
        SettlementApplication app = new SettlementApplication();
        app.setResidentAccount(accounts.getReferenceById(user.id()));
        app.setDormitory(dormitories.findById(dormitoryId)
                .orElseThrow(() -> ApiException.notFound("Гуртожиток не знайдено")));
        app.setStatus(ApplicationStatus.PENDING);
        app.setCreatedAt(LocalDateTime.now());
        app = applications.save(app);
        return new SettlementDtos.ApplicationResponse(app.getId(), dormitoryId, app.getStatus());
    }

    @Transactional(readOnly = true)
    public List<SettlementDtos.ApplicationResponse> mine(AuthenticatedUser user) {
        return applications.findByResidentAccountIdOrderByCreatedAtDesc(user.id()).stream()
                .map(a -> new SettlementDtos.ApplicationResponse(a.getId(), a.getDormitory().getId(), a.getStatus()))
                .toList();
    }
}
