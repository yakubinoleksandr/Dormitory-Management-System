package ua.kpi.dormitory.inventory;

/**
 * Перерахунок статусу кімнати з фактичної кількості зайнятих місць (лаб. 2, п. 5.2).
 * REPAIR — окремий режим, який не змінюється автоматично.
 */
public final class RoomStatusCalculator {
    private RoomStatusCalculator() { }

    public static RoomStatus recalculate(RoomStatus current, int capacity, long occupied) {
        if (current == RoomStatus.REPAIR) {
            return RoomStatus.REPAIR;
        }
        if (occupied <= 0) {
            return RoomStatus.FREE;
        }
        return occupied >= capacity ? RoomStatus.FULL : RoomStatus.PARTIAL;
    }
}
