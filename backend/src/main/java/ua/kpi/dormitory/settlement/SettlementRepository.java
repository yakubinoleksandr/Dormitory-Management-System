package ua.kpi.dormitory.settlement;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    boolean existsByContractNumber(String contractNumber);

    boolean existsByResidentAccountIdAndStatus(Long accountId, SettlementStatus status);

    boolean existsByBedIdAndStatus(Long bedId, SettlementStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Settlement s where s.id = :id")
    Optional<Settlement> lockById(@Param("id") Long id);

    @Query("select s.residentAccount.id from Settlement s where s.id = :id")
    Optional<Long> findResidentIdById(@Param("id") Long id);

    @Query("select s.bed.room.id from Settlement s where s.id = :id")
    Optional<Long> findRoomIdById(@Param("id") Long id);

    @Query("select s.bed.room.dormitory.id from Settlement s where s.id = :id")
    Optional<Long> findDormitoryIdById(@Param("id") Long id);

    @Query("""
            select count(s) > 0 from Settlement s
            where s.residentAccount.id = :accountId
              and s.status = ua.kpi.dormitory.settlement.SettlementStatus.ACTIVE
              and s.bed.room.dormitory.id = :dormitoryId""")
    boolean existsActiveInDormitory(@Param("accountId") Long accountId, @Param("dormitoryId") Long dormitoryId);

    @Query("""
            select count(s) > 0 from Settlement s
            where s.residentAccount.id = :accountId
              and s.status = ua.kpi.dormitory.settlement.SettlementStatus.ACTIVE
              and s.bed.room.id = :roomId""")
    boolean existsActiveInRoom(@Param("accountId") Long accountId, @Param("roomId") Long roomId);
}
