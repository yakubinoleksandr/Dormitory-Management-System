package ua.kpi.dormitory.security;

/** Користувач, витягнутий із перевіреного JWT (id береться з токена, а не з тіла запиту). */
public record AuthenticatedUser(Long id, String username, String role) {
    public boolean isAdmin() { return "ROLE_ADMIN".equals(role); }
    public boolean isManager() { return "ROLE_MANAGER".equals(role); }
    public boolean isResident() { return "ROLE_RESIDENT".equals(role); }
}
