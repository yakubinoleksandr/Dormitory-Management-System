package ua.kpi.dormitory.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * FR-02: генерація кімнат і ліжко-місць. Залежить від абстракції RoomLayoutGenerator (DIP):
 * потрібна реалізація підбирається за layout_type гуртожитку.
 */
@Service
public class RoomGenerationService {
    private final DormitoryService dormitoryService;
    private final RoomRepository rooms;
    private final BedRepository beds;
    private final Map<LayoutType, RoomLayoutGenerator> generators = new EnumMap<>(LayoutType.class);

    public RoomGenerationService(DormitoryService dormitoryService, RoomRepository rooms, BedRepository beds,
                                 List<RoomLayoutGenerator> generatorBeans) {
        this.dormitoryService = dormitoryService;
        this.rooms = rooms;
        this.beds = beds;
        generatorBeans.forEach(g -> generators.put(g.supports(), g));
    }

    @Transactional
    public int generate(Long dormitoryId, InventoryDtos.GenerateRoomsRequest req) {
        Dormitory dormitory = dormitoryService.getOrThrow(dormitoryId);
        if (rooms.existsByDormitoryId(dormitoryId)) {
            throw ApiException.conflict("Структура приміщень уже згенерована; повторний запуск не дублює її");
        }
        if (dormitory.getTotalFloors() != null && req.floors() > dormitory.getTotalFloors()) {
            throw ApiException.badRequest("Кількість поверхів перевищує total_floors гуртожитку");
        }
        RoomLayoutGenerator generator = generators.get(dormitory.getLayoutType());
        List<RoomSpec> specs;
        try {
            specs = generator.generate(new LayoutParams(req.floors(), req.blocksPerFloor(), req.roomsPerUnit(), req.capacity()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(e.getMessage());
        }
        for (RoomSpec spec : specs) {
            Room room = new Room();
            room.setDormitory(dormitory);
            room.setFloor(spec.floor());
            room.setRoomNumber(spec.roomNumber());
            room.setBlockName(spec.blockName());
            room.setCapacity(spec.capacity());
            room.setStatus(RoomStatus.FREE);
            room = rooms.save(room);
            for (int n = 1; n <= spec.capacity(); n++) {
                Bed bed = new Bed();
                bed.setRoom(room);
                bed.setBedNumber(n);
                beds.save(bed);
            }
        }
        return specs.size();
    }
}
