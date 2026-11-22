package ua.kpi.dormitory.inventory;

import java.util.List;

/**
 * Стратегія генерації кімнат для типу планування (FR-02). Новий тип планування = нова реалізація,
 * без зміни наявних класів (Open/Closed).
 */
public interface RoomLayoutGenerator {
    LayoutType supports();

    List<RoomSpec> generate(LayoutParams params);
}
