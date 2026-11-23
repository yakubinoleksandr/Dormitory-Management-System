package ua.kpi.dormitory.profile;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.kpi.dormitory.security.CurrentUser;

@RestController
@RequestMapping("/api")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping("/profile/me")
    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    public ProfileDtos.ProfileResponse me() {
        return service.get(CurrentUser.get().id());
    }

    @PutMapping("/residents/{accountId}/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')")
    public ProfileDtos.ProfileResponse update(@PathVariable Long accountId,
                                              @Valid @RequestBody ProfileDtos.UpdateProfileRequest request) {
        return service.update(CurrentUser.get(), accountId, request);
    }
}
