package ua.kpi.dormitory.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DormitoryManagerRepository extends JpaRepository<DormitoryManager, Long> {
    boolean existsByAccountIdAndDormitoryId(Long accountId, Long dormitoryId);

    @Query("select m.dormitory.id from DormitoryManager m where m.account.id = :accountId")
    List<Long> findDormitoryIdsByAccountId(@Param("accountId") Long accountId);
}
