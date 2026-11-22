package ua.kpi.dormitory.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.AuthenticatedUser;

import java.util.List;
import java.util.Map;

/** FR-18 (частково): JSON-списки. Експорт в Excel/PDF та глобальний пошук FR-17 — наступні ітерації. */
@Service
public class ReportService {
    private final ReportRepository reports;
    private final AccessPolicy access;

    public ReportService(ReportRepository reports, AccessPolicy access) {
        this.reports = reports;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public List<ResidentRow> residents(AuthenticatedUser manager, Long dormitoryId) {
        access.requireManagerOf(manager, dormitoryId);
        return reports.activeResidents(dormitoryId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> freeBeds(AuthenticatedUser manager, Long dormitoryId) {
        access.requireManagerOf(manager, dormitoryId);
        return reports.freeBeds(dormitoryId).stream()
                .map(row -> Map.<String, Object>of("roomNumber", row[0], "bedNumber", row[1])).toList();
    }
}
