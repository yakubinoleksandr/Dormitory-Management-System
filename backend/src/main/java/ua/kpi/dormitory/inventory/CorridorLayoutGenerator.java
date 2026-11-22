package ua.kpi.dormitory.inventory;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Коридорний тип: N поверхів × M кімнат на поверх. Номер кімнати: поверх*100 + порядковий номер (301, 302…). */
@Component
public class CorridorLayoutGenerator implements RoomLayoutGenerator {
    @Override
    public LayoutType supports() { return LayoutType.CORRIDOR; }

    @Override
    public List<RoomSpec> generate(LayoutParams p) {
        List<RoomSpec> rooms = new ArrayList<>();
        for (int floor = 1; floor <= p.floors(); floor++) {
            for (int m = 1; m <= p.roomsPerUnit(); m++) {
                rooms.add(new RoomSpec(floor, String.valueOf(floor * 100 + m), null, p.capacity()));
            }
        }
        return rooms;
    }
}
