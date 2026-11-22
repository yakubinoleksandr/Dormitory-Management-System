package ua.kpi.dormitory.repair;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    List<RepairRequest> findByResidentAccountIdOrderByCreatedAtDesc(Long accountId);

    List<RepairRequest> findByRoomDormitoryIdOrderByCreatedAtDesc(Long dormitoryId);

    /** Захист від одночасних переходів: блокування запису + повторна перевірка статусу. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RepairRequest r where r.id = :id")
    Optional<RepairRequest> lockById(@Param("id") Long id);

    @Query("select r.room.dormitory.id from RepairRequest r where r.id = :id")
    Optional<Long> findDormitoryIdById(@Param("id") Long id);
}
