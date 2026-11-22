package ua.kpi.dormitory.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ua.kpi.dormitory.account.AccountRepository;
import ua.kpi.dormitory.account.UserSession;
import ua.kpi.dormitory.account.UserSessionRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** Після перевірки підпису й терміну дії токена перевіряється активність запису user_sessions (лаб. 2, п. 10.1). */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserSessionRepository sessions;
    private final AccountRepository accounts;

    public JwtAuthenticationFilter(JwtService jwt, UserSessionRepository sessions, AccountRepository accounts) {
        this.jwt = jwt;
        this.sessions = sessions;
        this.accounts = accounts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authenticate(header.substring(7));
        }
        chain.doFilter(request, response);
    }

    private void authenticate(String token) {
        jwt.parse(token).ifPresent((Claims claims) ->
                sessions.findBySessionTokenAndActiveTrue(token)
                        .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                        .ifPresent(session -> accept(claims, session)));
    }

    private void accept(Claims claims, UserSession session) {
        Long accountId = Long.valueOf(claims.getSubject());
        accounts.findById(accountId).filter(a -> a.isActive()).ifPresent(account -> {
            String role = claims.get("role", String.class);
            var principal = new AuthenticatedUser(account.getId(), account.getUsername(), role);
            var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority(role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        });
    }
}
