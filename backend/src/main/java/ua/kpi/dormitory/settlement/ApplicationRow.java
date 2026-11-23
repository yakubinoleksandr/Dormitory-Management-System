package ua.kpi.dormitory.settlement;

import java.time.LocalDateTime;

/** Рядок списку заяв для завідуючої. */
public record ApplicationRow(Long id, Long residentAccountId, String fullName,
                             ApplicationStatus status, LocalDateTime createdAt) { }
