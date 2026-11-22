package ua.kpi.dormitory.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResidentProfileRepository extends JpaRepository<ResidentProfile, Long> {
    Optional<ResidentProfile> findByAccountId(Long accountId);
}
