package ua.kpi.dormitory.inventory;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Блочний тип: N поверхів × K блоків × M кімнат. Блок «505», кімнати «505/1», «505/2»… */
@Component
public class BlockLayoutGenerator implements RoomLayoutGenerator {
    @Override
    public LayoutType supports() { return LayoutType.BLOCK; }

    @Override
    public List<RoomSpec> generate(LayoutParams p) {
        if (p.blocksPerFloor() < 1 || p.blocksPerFloor() > 99) {
            throw new IllegalArgumentException("Для блочного типу 1 <= blocksPerFloor <= 99");
        }
        List<RoomSpec> rooms = new ArrayList<>();
        for (int floor = 1; floor <= p.floors(); floor++) {
            for (int k = 1; k <= p.blocksPerFloor(); k++) {
                String block = String.valueOf(floor * 100 + k);
                for (int m = 1; m <= p.roomsPerUnit(); m++) {
                    rooms.add(new RoomSpec(floor, block + "/" + m, block, p.capacity()));
                }
            }
        }
        return rooms;
    }
}
