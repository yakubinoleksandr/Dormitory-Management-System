package ua.kpi.dormitory.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomIdOrderByBedNumber(Long roomId);

    long countByRoomIdAndOccupiedTrue(Long roomId);

    /** Скалярний запит: id кімнати без завантаження сутності Bed (щоб читати стан місця вже після блокування кімнати). */
    @Query("select b.room.id from Bed b where b.id = :bedId")
    Optional<Long> findRoomIdByBedId(@Param("bedId") Long bedId);
}
