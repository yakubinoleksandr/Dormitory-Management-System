package ua.kpi.dormitory.account;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.profile.ResidentProfile;
import ua.kpi.dormitory.profile.ResidentProfileRepository;

import java.time.LocalDateTime;

/** FR-20: адміністратор створює облікові записи; для мешканця одразу створюється профіль (зв'язок 1:1). */
@Service
public class AccountService {
    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final ResidentProfileRepository profiles;

    public AccountService(AccountRepository accounts, RoleRepository roles, PasswordEncoder encoder,
                          ResidentProfileRepository profiles) {
        this.accounts = accounts;
        this.roles = roles;
        this.encoder = encoder;
        this.profiles = profiles;
    }

    @Transactional
    public AccountDtos.AccountResponse createAccount(AccountDtos.CreateAccountRequest req) {
        if (!Role.MANAGER.equals(req.role()) && !Role.RESIDENT.equals(req.role())) {
            throw ApiException.badRequest("Дозволені ролі: " + Role.MANAGER + ", " + Role.RESIDENT);
        }
        boolean resident = Role.RESIDENT.equals(req.role());
        if (resident && (req.fullName() == null || req.fullName().isBlank())) {
            throw ApiException.badRequest("Для мешканця потрібне ПІБ (fullName)");
        }
        if (accounts.existsByUsername(req.username()) || accounts.existsByEmail(req.email())) {
            throw ApiException.conflict("Логін або email уже використовується");
        }
        Role role = roles.findByName(req.role()).orElseThrow(() -> ApiException.notFound("Роль не знайдено"));
        Account account = new Account();
        account.setRole(role);
        account.setUsername(req.username());
        account.setEmail(req.email());
        account.setPasswordHash(encoder.encode(req.password()));
        account.setCreatedAt(LocalDateTime.now());
        account = accounts.save(account);
        if (resident) {
            ResidentProfile profile = new ResidentProfile();
            profile.setAccount(account);
            profile.setFullName(req.fullName());
            profile.setCourse(req.course());
            profile.setGroupName(req.groupName());
            profile.setPhone(req.phone());
            profile.setBenefitsInfo(req.benefitsInfo());
            profiles.save(profile);
        }
        return new AccountDtos.AccountResponse(account.getId(), account.getUsername(), account.getEmail(), role.getName());
    }
}
