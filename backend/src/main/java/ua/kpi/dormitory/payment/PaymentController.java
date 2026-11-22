package ua.kpi.dormitory.payment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.dormitory.security.CurrentUser;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) { this.service = service; }

    @PostMapping(value = "/settlements/{id}/payments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDtos.PaymentResponse submit(@PathVariable Long id,
                                              @RequestParam BigDecimal amount,
                                              @RequestParam(required = false) String method,
                                              @RequestParam("receipt") MultipartFile receipt) {
        return service.submit(CurrentUser.get(), id, amount, method, receipt);
    }

    @PatchMapping("/payments/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public PaymentDtos.PaymentResponse updateStatus(@PathVariable Long id,
                                                    @Valid @RequestBody PaymentDtos.UpdateStatusRequest request) {
        return service.updateStatus(CurrentUser.get(), id, request.status());
    }
}
