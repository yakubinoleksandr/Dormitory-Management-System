package ua.kpi.dormitory.payment;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PaymentDtos {
    private PaymentDtos() { }

    public record PaymentResponse(Long id, Long settlementId, BigDecimal amount, String paymentMethod,
                                  PaymentStatus status, LocalDateTime paidAt) { }

    public record UpdateStatusRequest(@NotNull PaymentStatus status) { }
}
