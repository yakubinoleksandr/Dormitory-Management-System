package ua.kpi.dormitory.settlement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.kpi.dormitory.security.CurrentUser;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SettlementController {
    public record ChangeBedRequest(@NotNull Long bedId) { }

    private final SettlementApplicationService applicationService;
    private final SettlementService settlementService;

    public SettlementController(SettlementApplicationService applicationService, SettlementService settlementService) {
        this.applicationService = applicationService;
        this.settlementService = settlementService;
    }

    @PostMapping("/settlement-applications")
    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementDtos.ApplicationResponse createApplication(@Valid @RequestBody SettlementDtos.CreateApplicationRequest request) {
        return applicationService.create(CurrentUser.get(), request.dormitoryId());
    }

    @GetMapping("/settlement-applications/mine")
    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    public List<SettlementDtos.ApplicationResponse> myApplications() {
        return applicationService.mine(CurrentUser.get());
    }

    @PostMapping("/settlements")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementDtos.SettlementResponse settle(@Valid @RequestBody SettlementDtos.SettleRequest request) {
        return settlementService.settle(CurrentUser.get(), request);
    }

    @PatchMapping("/settlements/{id}/move-out")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public SettlementDtos.SettlementResponse moveOut(@PathVariable Long id) {
        return settlementService.moveOut(CurrentUser.get(), id);
    }

    @PatchMapping("/settlements/{id}/bed")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public SettlementDtos.SettlementResponse changeBed(@PathVariable Long id, @Valid @RequestBody ChangeBedRequest request) {
        return settlementService.changeBed(CurrentUser.get(), id, request.bedId());
    }

    @GetMapping("/settlement-applications")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<ApplicationRow> applications(@RequestParam Long dormitoryId,
                                             @RequestParam(defaultValue = "PENDING") ApplicationStatus status) {
        return applicationService.forDormitory(CurrentUser.get(), dormitoryId, status);
    }

    @PatchMapping("/settlement-applications/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public SettlementDtos.ApplicationResponse reject(@PathVariable Long id) {
        return settlementService.reject(CurrentUser.get(), id);
    }

    @PatchMapping("/settlements/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public SettlementDtos.SettlementResponse updateTerms(@PathVariable Long id,
                                                         @Valid @RequestBody SettlementDtos.UpdateTermsRequest request) {
        return settlementService.updateTerms(CurrentUser.get(), id, request);
    }
}
