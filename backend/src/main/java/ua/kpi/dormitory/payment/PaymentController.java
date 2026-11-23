package ua.kpi.dormitory.payment;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.dormitory.security.CurrentUser;

import java.math.BigDecimal;
import java.util.List;

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

    @GetMapping("/settlements/{id}/payments")
    @PreAuthorize("hasAnyAuthority('ROLE_RESIDENT','ROLE_MANAGER')")
    public List<PaymentDtos.PaymentResponse> forSettlement(@PathVariable Long id) {
        return service.forSettlement(CurrentUser.get(), id);
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<PaymentDtos.PaymentResponse> byStatus(@RequestParam Long dormitoryId,
                                                      @RequestParam(defaultValue = "PENDING") PaymentStatus status) {
        return service.byStatus(CurrentUser.get(), dormitoryId, status);
    }

    @GetMapping("/payments/{id}/receipt")
    @PreAuthorize("hasAnyAuthority('ROLE_RESIDENT','ROLE_MANAGER')")
    public ResponseEntity<Resource> receipt(@PathVariable Long id) {
        Resource file = service.receipt(CurrentUser.get(), id);
        // тип за розширенням файлу (.pdf, .jpg, .png), інакше браузер покаже PDF як текст
        MediaType type = MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(file);
    }
}