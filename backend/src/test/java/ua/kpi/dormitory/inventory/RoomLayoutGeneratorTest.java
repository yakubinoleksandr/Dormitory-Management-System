package ua.kpi.dormitory.inventory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomLayoutGeneratorTest {
    @Test
    void corridorGeneratesFloorsTimesRooms() {
        List<RoomSpec> rooms = new CorridorLayoutGenerator().generate(new LayoutParams(3, 0, 10, 4));
        assertEquals(30, rooms.size());
        assertEquals("101", rooms.get(0).roomNumber());
        assertNull(rooms.get(0).blockName());
        assertEquals("310", rooms.get(29).roomNumber());
    }

    @Test
    void blockGeneratesFloorsTimesBlocksTimesRooms() {
        List<RoomSpec> rooms = new BlockLayoutGenerator().generate(new LayoutParams(2, 3, 2, 3));
        assertEquals(12, rooms.size());
        assertEquals("101/1", rooms.get(0).roomNumber());
        assertEquals("101", rooms.get(0).blockName());
        assertEquals("203/2", rooms.get(11).roomNumber());
    }

    @Test
    void capacityOutOfRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new LayoutParams(1, 1, 1, 5));
    }
}
