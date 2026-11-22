package ua.kpi.dormitory.repair;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.kpi.dormitory.security.CurrentUser;

import java.util.List;

@RestController
@RequestMapping("/api/repair-requests")
public class RepairController {
    private final RepairService service;

    public RepairController(RepairService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public RepairDtos.RepairResponse create(@Valid @RequestBody RepairDtos.CreateRepairRequest request) {
        return service.create(CurrentUser.get(), request);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    public List<RepairDtos.RepairResponse> mine() {
        return service.mine(CurrentUser.get());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<RepairDtos.RepairResponse> byDormitory(@RequestParam Long dormitoryId) {
        return service.byDormitory(CurrentUser.get(), dormitoryId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public RepairDtos.RepairResponse changeStatus(@PathVariable Long id, @Valid @RequestBody RepairDtos.ChangeStatusRequest request) {
        return service.changeStatus(CurrentUser.get(), id, request);
    }
}
