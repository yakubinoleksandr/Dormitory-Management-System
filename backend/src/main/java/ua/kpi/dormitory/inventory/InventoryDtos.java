package ua.kpi.dormitory.inventory;

import jakarta.validation.constraints.*;

public final class InventoryDtos {
    private InventoryDtos() { }

    public record CreateDormitoryRequest(
            @NotBlank @Size(max = 100) String nameOrNumber,
            @Size(max = 255) String address,
            @NotNull @Min(1) @Max(50) Integer totalFloors,
            @NotNull LayoutType layoutType) { }

    public record DormitoryResponse(Long id, String nameOrNumber, String address, Integer totalFloors, LayoutType layoutType) { }

    public record GenerateRoomsRequest(
            @Min(1) @Max(50) int floors,
            @Min(0) @Max(99) int blocksPerFloor,
            @Min(1) @Max(99) int roomsPerUnit,
            @Min(1) @Max(4) int capacity) { }

    public record RoomResponse(Long id, int floor, String roomNumber, String blockName,
                               int capacity, RoomStatus status, long freeBeds) { }

    public record BedResponse(Long id, int bedNumber, boolean occupied) { }
}
