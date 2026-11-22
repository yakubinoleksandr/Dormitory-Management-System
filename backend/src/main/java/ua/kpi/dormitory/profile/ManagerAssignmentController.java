package ua.kpi.dormitory.profile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dormitories/{dormitoryId}/managers")
public class ManagerAssignmentController {
    public record AssignRequest(@NotNull Long managerAccountId) { }

    private final ManagerAssignmentService service;

    public ManagerAssignmentController(ManagerAssignmentService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public void assign(@PathVariable Long dormitoryId, @Valid @RequestBody AssignRequest request) {
        service.assign(request.managerAccountId(), dormitoryId);
    }
}
