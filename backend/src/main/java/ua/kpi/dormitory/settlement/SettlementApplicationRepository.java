package ua.kpi.dormitory.settlement;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementApplicationRepository extends JpaRepository<SettlementApplication, Long> {
    List<SettlementApplication> findByResidentAccountIdOrderByCreatedAtDesc(Long accountId);

    boolean existsByResidentAccountIdAndDormitoryIdAndStatus(Long accountId, Long dormitoryId, ApplicationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from SettlementApplication a where a.id = :id")
    Optional<SettlementApplication> lockById(@Param("id") Long id);

    /** Скалярний запит для визначення власника заяви до блокування. */
    @Query("select a.residentAccount.id from SettlementApplication a where a.id = :id")
    Optional<Long> findResidentIdById(@Param("id") Long id);
}
