package ua.kpi.dormitory.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** FR-04/FR-05: карта номерного фонду з колірним статусом (звичайне читання без блокувань). */
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
        List<Room> list = floor == null
                ? rooms.findByDormitoryIdOrderByFloorAscRoomNumberAsc(dormitoryId)
                : rooms.findByDormitoryIdAndFloorOrderByRoomNumberAsc(dormitoryId, floor);
        // TODO(NFR-01): замінити на один агрегований запит замість count на кожну кімнату.
        return list.stream().map(r -> new InventoryDtos.RoomResponse(
                r.getId(), r.getFloor(), r.getRoomNumber(), r.getBlockName(), r.getCapacity(), r.getStatus(),
                r.getCapacity() - beds.countByRoomIdAndOccupiedTrue(r.getId()))).toList();
    }
}
