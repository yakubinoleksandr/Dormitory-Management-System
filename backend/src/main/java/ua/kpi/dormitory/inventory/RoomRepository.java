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
}
