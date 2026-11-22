package ua.kpi.dormitory.inventory;

/**
 * Параметри генерації номерного фонду.
 * @param floors          кількість поверхів (N)
 * @param blocksPerFloor  кількість блоків на поверсі (K); ігнорується для CORRIDOR
 * @param roomsPerUnit    кімнат на поверсі (CORRIDOR) або на блок (BLOCK) (M)
 * @param capacity        ліжко-місць у кімнаті, 1..4
 */
public record LayoutParams(int floors, int blocksPerFloor, int roomsPerUnit, int capacity) {
    public LayoutParams {
        if (floors < 1 || roomsPerUnit < 1 || roomsPerUnit > 99) {
            throw new IllegalArgumentException("floors >= 1, 1 <= roomsPerUnit <= 99");
        }
        if (capacity < 1 || capacity > 4) {
            throw new IllegalArgumentException("capacity має бути в межах 1..4");
        }
    }
}
