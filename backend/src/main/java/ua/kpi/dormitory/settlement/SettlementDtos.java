package ua.kpi.dormitory.settlement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class SettlementDtos {
    private SettlementDtos() { }

    public record CreateApplicationRequest(@NotNull Long dormitoryId) { }

    public record ApplicationResponse(Long id, Long dormitoryId, ApplicationStatus status) { }

    public record SettleRequest(
            @NotNull Long applicationId,
            @NotNull Long bedId,
            @NotBlank @Size(max = 50) String contractNumber,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate) { }

    public record SettlementResponse(Long id, Long applicationId, Long residentAccountId, Long bedId,
                                     String contractNumber, LocalDate startDate, LocalDate endDate,
                                     SettlementStatus status) { }
}
