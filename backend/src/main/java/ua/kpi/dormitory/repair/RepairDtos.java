package ua.kpi.dormitory.repair;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class RepairDtos {
    private RepairDtos() { }

    /** Кімната має збігатися з кімнатою активного договору мешканця (перевіряє RepairService). */
    public record CreateRepairRequest(@NotNull Long roomId, @NotNull RepairCategory category, @NotBlank String description) { }

    public record ChangeStatusRequest(@NotNull RepairStatus status, String managerComment) { }

    public record RepairResponse(Long id, Long roomId, RepairCategory category, String description,
                                 RepairStatus status, String managerComment,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) { }
}
