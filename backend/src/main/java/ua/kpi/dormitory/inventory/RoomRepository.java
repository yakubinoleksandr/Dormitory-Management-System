package ua.kpi.dormitory.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByDormitoryId(Long dormitoryId);

    List<Room> findByDormitoryIdOrderByFloorAscRoomNumberAsc(Long dormitoryId);

    List<Room> findByDormitoryIdAndFloorOrderByRoomNumberAsc(Long dormitoryId, int floor);

    /** Блокування кімнати серіалізує зміну її місць і перерахунок статусу. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> lockById(@Param("id") Long id);

    /**
     * Один агрегований запит для карти (NFR-01): кімната + кількість зайнятих місць.
     * Рядок: id, floor, roomNumber, blockName, capacity, status, occupiedCount.
     */
    @Query("""
            select r.id, r.floor, r.roomNumber, r.blockName, r.capacity, r.status,
                   coalesce(sum(case when b.occupied = true then 1 else 0 end), 0)
            from Room r left join Bed b on b.room = r
            where r.dormitory.id = :dormitoryId and (cast(:floor as integer) is null or r.floor = :floor)
            group by r.id, r.floor, r.roomNumber, r.blockName, r.capacity, r.status
            order by r.floor, r.roomNumber""")
    List<Object[]> mapRows(@Param("dormitoryId") Long dormitoryId, @Param("floor") Integer floor);
}
