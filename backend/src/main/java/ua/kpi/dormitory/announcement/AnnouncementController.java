package ua.kpi.dormitory.announcement;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.kpi.dormitory.security.CurrentUser;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementDtos.AnnouncementResponse create(@Valid @RequestBody AnnouncementDtos.CreateAnnouncementRequest request) {
        return service.create(CurrentUser.get(), request);
    }

    @GetMapping
    public List<AnnouncementDtos.AnnouncementResponse> active(@RequestParam Long dormitoryId) {
        return service.active(CurrentUser.get(), dormitoryId);
    }
}
