package ua.kpi.dormitory.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AccountDtos {
    private AccountDtos() { }

    public record CreateAccountRequest(
            @NotBlank @Size(max = 50) String username,
            @NotBlank @Email @Size(max = 100) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank String role,
            @Size(max = 150) String fullName,
            Integer course,
            @Size(max = 20) String groupName,
            @Size(max = 20) String phone,
            String benefitsInfo) { }

    public record AccountResponse(Long id, String username, String email, String role) { }
}
