package ua.kpi.dormitory.search;

import java.util.List;

public final class SearchDtos {
    private SearchDtos() { }

    public record SearchResponse(List<ResidentHit> residents, List<RoomHit> rooms, List<DormitoryHit> dormitories) { }
}
