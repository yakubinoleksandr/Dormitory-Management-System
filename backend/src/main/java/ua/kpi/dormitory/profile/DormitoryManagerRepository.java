package ua.kpi.dormitory.profile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DormitoryManagerRepository extends JpaRepository<DormitoryManager, Long> {
    boolean existsByAccountIdAndDormitoryId(Long accountId, Long dormitoryId);
}
