package ua.kpi.dormitory.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySettlementIdOrderByCreatedAtDesc(Long settlementId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p "
            + "where p.settlement.id = :settlementId and p.status = ua.kpi.dormitory.payment.PaymentStatus.VERIFIED")
    BigDecimal sumVerified(@Param("settlementId") Long settlementId);

    @Query("select p from Payment p where p.settlement.bed.room.dormitory.id = :dormitoryId and p.status = :status "
            + "order by p.createdAt")
    List<Payment> findByDormitoryAndStatus(@Param("dormitoryId") Long dormitoryId, @Param("status") PaymentStatus status);
}
