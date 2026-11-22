package ua.kpi.dormitory.inventory;

/** Опис кімнати, яку потрібно створити. */
public record RoomSpec(int floor, String roomNumber, String blockName, int capacity) { }
