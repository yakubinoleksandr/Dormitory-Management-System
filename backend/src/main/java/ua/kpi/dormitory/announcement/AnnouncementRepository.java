package ua.kpi.dormitory.announcement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    /** Актуальні оголошення в хронологічному порядку (FR-12): без дати втрати актуальності або ще не прострочені. */
    @Query("select a from Announcement a where a.dormitory.id = :dormitoryId "
            + "and (a.expiresAt is null or a.expiresAt > :now) order by a.createdAt desc")
    List<Announcement> findActive(@Param("dormitoryId") Long dormitoryId, @Param("now") LocalDateTime now);
}
