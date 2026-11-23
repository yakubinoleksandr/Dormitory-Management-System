package ua.kpi.dormitory.report;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /** Експорт: GET /api/reports/residents/export?dormitoryId=1&format=xlsx|pdf */
    @GetMapping("/{type}/export")
    public ResponseEntity<byte[]> export(@PathVariable String type, @RequestParam Long dormitoryId,
                                         @RequestParam(defaultValue = "xlsx") String format) {
        ReportExporter exporter = service.exporter(format);
        ReportTable table = service.table(CurrentUser.get(), dormitoryId, type);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exporter.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + type + "." + exporter.format() + "\"")
                .body(exporter.export(table));
    }
}
