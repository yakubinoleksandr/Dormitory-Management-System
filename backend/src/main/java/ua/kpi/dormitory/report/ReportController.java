package ua.kpi.dormitory.report;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.kpi.dormitory.security.CurrentUser;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAuthority('ROLE_MANAGER')")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/residents")
    public List<ResidentRow> residents(@RequestParam Long dormitoryId) {
        return service.residents(CurrentUser.get(), dormitoryId);
    }

    @GetMapping("/free-beds")
    public List<Map<String, Object>> freeBeds(@RequestParam Long dormitoryId) {
        return service.freeBeds(CurrentUser.get(), dormitoryId);
    }
}
