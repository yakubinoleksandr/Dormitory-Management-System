package ua.kpi.dormitory.payment;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.AuthenticatedUser;
import ua.kpi.dormitory.settlement.Settlement;
import ua.kpi.dormitory.settlement.SettlementRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** FR-16: мешканець подає квитанцію, завідуюча верифікує. Точний борг не обчислюється (немає тарифів у схемі). */
@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final SettlementRepository settlements;
    private final FileStorage storage;
    private final AccessPolicy access;

    public PaymentService(PaymentRepository payments, SettlementRepository settlements,
                          FileStorage storage, AccessPolicy access) {
        this.payments = payments;
        this.settlements = settlements;
        this.storage = storage;
        this.access = access;
    }

    @Transactional
    public PaymentDtos.PaymentResponse submit(AuthenticatedUser resident, Long settlementId, BigDecimal amount,
                                              String method, MultipartFile receipt) {
        Settlement settlement = settlements.findById(settlementId)
                .orElseThrow(() -> ApiException.notFound("Договір не знайдено"));
        if (!settlement.getResidentAccount().getId().equals(resident.id())) {
            throw ApiException.forbidden("Можна подавати квитанції лише за власним договором");
        }
        if (amount == null || amount.signum() <= 0) {
            throw ApiException.badRequest("Сума має бути додатною");
        }
        String path = storage.store(receipt);
        try {
            Payment p = new Payment();
            p.setSettlement(settlement);
            p.setAmount(amount);
            p.setPaymentMethod(method);
            p.setReceiptUrl(path);
            p.setStatus(PaymentStatus.PENDING);
            p.setPaidAt(LocalDateTime.now());
            p.setCreatedAt(LocalDateTime.now());
            return toResponse(payments.save(p));
        } catch (RuntimeException e) {
            storage.delete(path);   // не залишаємо непов'язаних квитанцій
            throw e;
        }
    }

    @Transactional
    public PaymentDtos.PaymentResponse updateStatus(AuthenticatedUser manager, Long paymentId, PaymentStatus status) {
        Payment p = payments.findById(paymentId).orElseThrow(() -> ApiException.notFound("Платіж не знайдено"));
        Long dormitoryId = settlements.findDormitoryIdById(p.getSettlement().getId()).orElseThrow();
        access.requireManagerOf(manager, dormitoryId);
        if (p.getStatus() != PaymentStatus.PENDING || status == PaymentStatus.PENDING) {
            throw ApiException.conflict("Перевірити можна лише платіж зі статусом PENDING");
        }
        p.setStatus(status);
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> forSettlement(AuthenticatedUser user, Long settlementId) {
        Settlement settlement = settlements.findById(settlementId)
                .orElseThrow(() -> ApiException.notFound("Договір не знайдено"));
        requireAccess(user, settlement);
        return payments.findBySettlementIdOrderByCreatedAtDesc(settlementId).stream().map(PaymentService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> byStatus(AuthenticatedUser manager, Long dormitoryId, PaymentStatus status) {
        access.requireManagerOf(manager, dormitoryId);
        return payments.findByDormitoryAndStatus(dormitoryId, status).stream().map(PaymentService::toResponse).toList();
    }

    /** Файл квитанції бачить власник платежу та завідуюча відповідного гуртожитку. */
    @Transactional(readOnly = true)
    public Resource receipt(AuthenticatedUser user, Long paymentId) {
        Payment p = payments.findById(paymentId).orElseThrow(() -> ApiException.notFound("Платіж не знайдено"));
        requireAccess(user, p.getSettlement());
        if (p.getReceiptUrl() == null) {
            throw ApiException.notFound("До платежу не додано квитанцію");
        }
        return storage.load(p.getReceiptUrl());
    }

    private void requireAccess(AuthenticatedUser user, Settlement settlement) {
        if (user.isResident()) {
            if (!settlement.getResidentAccount().getId().equals(user.id())) {
                throw ApiException.forbidden("Немає доступу до цього договору");
            }
            return;
        }
        Long dormitoryId = settlements.findDormitoryIdById(settlement.getId()).orElseThrow();
        access.requireManagerOf(user, dormitoryId);
    }

    private static PaymentDtos.PaymentResponse toResponse(Payment p) {
        return new PaymentDtos.PaymentResponse(p.getId(), p.getSettlement().getId(), p.getAmount(),
                p.getPaymentMethod(), p.getStatus(), p.getPaidAt());
    }
}
