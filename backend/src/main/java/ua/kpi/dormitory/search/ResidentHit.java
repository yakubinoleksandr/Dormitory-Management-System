package ua.kpi.dormitory.search;

import ua.kpi.dormitory.settlement.SettlementStatus;

public record ResidentHit(Long accountId, String fullName, String contractNumber, String roomNumber,
                          String dormitoryName, SettlementStatus status) { }
