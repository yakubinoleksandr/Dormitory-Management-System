package ua.kpi.dormitory.settlement;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("select s.bed.room.dormitory.id from Settlement s where s.residentAccount.id = :accountId")
    List<Long> findDormitoryIdsByResident(@Param("accountId") Long accountId);

    Optional<Settlement> findByResidentAccountIdAndStatus(Long accountId, SettlementStatus status);

    @Query("select s.residentAccount.id from Settlement s where s.id = :id")
    Optional<Long> findResidentAccountId(@Param("id") Long id);

    /** Сусіди по блоку: лише ПІБ (NFR-04, FR-10). */
    @Query("""
            select p.fullName from Settlement s
              join s.bed b join b.room r
              join ua.kpi.dormitory.profile.ResidentProfile p on p.account = s.residentAccount
            where s.status = ua.kpi.dormitory.settlement.SettlementStatus.ACTIVE
              and r.dormitory.id = :dormitoryId and r.blockName = :blockName
              and s.residentAccount.id <> :selfId
            order by p.fullName""")
    List<String> neighbourNamesInBlock(@Param("dormitoryId") Long dormitoryId, @Param("blockName") String blockName,
                                       @Param("selfId") Long selfId);

    /** Сусіди по кімнаті (коридорний тип). */
    @Query("""
            select p.fullName from Settlement s
              join s.bed b
              join ua.kpi.dormitory.profile.ResidentProfile p on p.account = s.residentAccount
            where s.status = ua.kpi.dormitory.settlement.SettlementStatus.ACTIVE
              and b.room.id = :roomId and s.residentAccount.id <> :selfId
            order by p.fullName""")
    List<String> neighbourNamesInRoom(@Param("roomId") Long roomId, @Param("selfId") Long selfId);
}
