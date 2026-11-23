package ua.kpi.dormitory.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Створює першого адміністратора при старті, якщо в БД ще немає жодного ROLE_ADMIN і задано
 * ADMIN_USERNAME / ADMIN_PASSWORD. Пароль зберігається як BCrypt-хеш; повторний запуск нічого не змінює.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(AccountRepository accounts, RoleRepository roles, PasswordEncoder encoder,
                          @Value("${app.admin.username:}") String username,
                          @Value("${app.admin.email:admin@localhost}") String email,
                          @Value("${app.admin.password:}") String password) {
        this.accounts = accounts;
        this.roles = roles;
        this.encoder = encoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank()) {
            return;
        }
        if (accounts.existsByRoleName(Role.ADMIN)) {
            return;
        }
        Role role = roles.findByName(Role.ADMIN).orElseThrow();
        Account admin = new Account();
        admin.setRole(role);
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(encoder.encode(password));
        admin.setCreatedAt(LocalDateTime.now());
        accounts.save(admin);
        log.info("Створено початкового адміністратора '{}'", username);
    }
}
