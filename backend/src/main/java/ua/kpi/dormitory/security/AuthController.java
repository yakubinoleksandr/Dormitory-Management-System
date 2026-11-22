package ua.kpi.dormitory.security;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String header) {
        if (header.startsWith("Bearer ")) {
            service.logout(header.substring(7));
        }
    }
}
