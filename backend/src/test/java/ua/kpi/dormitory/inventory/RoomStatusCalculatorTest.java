package ua.kpi.dormitory.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomStatusCalculatorTest {
    @Test
    void emptyRoomIsFree() {
        assertEquals(RoomStatus.FREE, RoomStatusCalculator.recalculate(RoomStatus.FREE, 4, 0));
    }

    @Test
    void partiallyOccupiedRoomIsPartial() {
        assertEquals(RoomStatus.PARTIAL, RoomStatusCalculator.recalculate(RoomStatus.FREE, 4, 3));
    }

    @Test
    void fullyOccupiedRoomIsFull() {
        assertEquals(RoomStatus.FULL, RoomStatusCalculator.recalculate(RoomStatus.PARTIAL, 2, 2));
    }

    @Test
    void repairModeIsPreserved() {
        assertEquals(RoomStatus.REPAIR, RoomStatusCalculator.recalculate(RoomStatus.REPAIR, 4, 0));
    }
}
