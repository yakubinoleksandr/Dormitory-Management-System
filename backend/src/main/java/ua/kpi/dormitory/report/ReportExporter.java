package ua.kpi.dormitory.report;

/**
 * Експорт звіту у певний формат (FR-18). Новий формат = нова реалізація; ReportService не змінюється (OCP, DIP).
 */
public interface ReportExporter {
    /** Значення параметра format, наприклад "xlsx" або "pdf". */
    String format();

    String contentType();

    byte[] export(ReportTable table);
}
