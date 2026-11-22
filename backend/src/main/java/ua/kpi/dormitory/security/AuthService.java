package ua.kpi.dormitory.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.account.Account;
import ua.kpi.dormitory.account.AccountRepository;
import ua.kpi.dormitory.account.UserSession;
import ua.kpi.dormitory.account.UserSessionRepository;
import ua.kpi.dormitory.common.ApiException;

import java.time.LocalDateTime;

/** FR-19: вхід за логіном і паролем, видача JWT із роллю, запис user_sessions. */
@Service
public class AuthService {
    private final AccountRepository accounts;
    private final UserSessionRepository sessions;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(AccountRepository accounts, UserSessionRepository sessions,
                       PasswordEncoder encoder, JwtService jwt) {
        this.accounts = accounts;
        this.sessions = sessions;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        Account account = accounts.findByUsername(req.username())
                .filter(Account::isActive)
                .filter(a -> encoder.matches(req.password(), a.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Невірний логін або пароль"));

        String role = account.getRole().getName();
        String token = jwt.issue(account.getId(), role);
        LocalDateTime now = LocalDateTime.now();

        UserSession session = new UserSession();
        session.setAccount(account);
        session.setSessionToken(token);
        session.setLastActivityAt(now);
        session.setExpiresAt(now.plusMinutes(jwt.ttlMinutes()));
        sessions.save(session);

        account.setLastLoginAt(now);
        return new AuthDtos.LoginResponse(token, role, jwt.ttlMinutes());
    }

    /** Вихід деактивує серверну сесію. */
    @Transactional
    public void logout(String token) {
        sessions.findBySessionTokenAndActiveTrue(token).ifPresent(s -> s.setActive(false));
    }
}
