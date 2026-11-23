package ua.kpi.dormitory.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** FR-04/FR-05: карта номерного фонду з колірним статусом; один запит замість запиту на кожну кімнату (NFR-01). */
@Service
public class RoomQueryService {
    private final RoomRepository rooms;
    private final BedRepository beds;

    public RoomQueryService(RoomRepository rooms, BedRepository beds) {
        this.rooms = rooms;
        this.beds = beds;
    }

    @Transactional(readOnly = true)
    public List<InventoryDtos.RoomResponse> map(Long dormitoryId, Integer floor) {
        return rooms.mapRows(dormitoryId, floor).stream().map(row -> {
            int capacity = ((Number) row[4]).intValue();
            long occupied = ((Number) row[6]).longValue();
            return new InventoryDtos.RoomResponse(((Number) row[0]).longValue(), ((Number) row[1]).intValue(),
                    (String) row[2], (String) row[3], capacity, (RoomStatus) row[5], capacity - occupied);
        }).toList();
    }

    /** Ліжка кімнати для вибору місця під час поселення (лише для завідуючої; перевірка в контролері). */
    @Transactional(readOnly = true)
    public List<InventoryDtos.BedResponse> beds(Long dormitoryId, Long roomId) {
        return beds.findByRoomIdOrderByBedNumber(roomId).stream()
                .filter(b -> b.getRoom().getDormitory().getId().equals(dormitoryId))
                .map(b -> new InventoryDtos.BedResponse(b.getId(), b.getBedNumber(), b.isOccupied())).toList();
    }
}
