package ua.kpi.dormitory.cabinet;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.kpi.dormitory.security.CurrentUser;

@RestController
@RequestMapping("/api/me")
public class CabinetController {
    private final CabinetService service;

    public CabinetController(CabinetService service) { this.service = service; }

    @GetMapping("/cabinet")
    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    public CabinetDtos.CabinetResponse cabinet() {
        return service.get(CurrentUser.get());
    }
}
