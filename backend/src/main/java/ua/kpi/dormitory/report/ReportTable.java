package ua.kpi.dormitory.report;

import java.util.List;

/** Формато-незалежний опис таблиці звіту. */
public record ReportTable(String title, List<String> headers, List<List<String>> rows) { }
