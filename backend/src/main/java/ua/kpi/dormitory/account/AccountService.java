package ua.kpi.dormitory.account;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;

import java.time.LocalDateTime;

/** FR-20: адміністратор створює облікові записи (завідуючих; первинний пароль передається вручну). */
@Service
public class AccountService {
    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public AccountService(AccountRepository accounts, RoleRepository roles, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public AccountDtos.AccountResponse createAccount(AccountDtos.CreateAccountRequest req) {
        if (!Role.MANAGER.equals(req.role()) && !Role.RESIDENT.equals(req.role())) {
            throw ApiException.badRequest("Дозволені ролі: " + Role.MANAGER + ", " + Role.RESIDENT);
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
        return new AccountDtos.AccountResponse(account.getId(), account.getUsername(), account.getEmail(), role.getName());
    }
}
