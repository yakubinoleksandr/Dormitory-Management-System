package ua.kpi.dormitory.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.CurrentUser;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dormitories")
public class InventoryController {
    private final DormitoryService dormitoryService;
    private final RoomGenerationService generationService;
    private final RoomQueryService queryService;
    private final AccessPolicy access;

    public InventoryController(DormitoryService dormitoryService, RoomGenerationService generationService,
                               RoomQueryService queryService, AccessPolicy access) {
        this.dormitoryService = dormitoryService;
        this.generationService = generationService;
        this.queryService = queryService;
        this.access = access;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDtos.DormitoryResponse create(@Valid @RequestBody InventoryDtos.CreateDormitoryRequest request) {
        return dormitoryService.create(request);
    }

    @PostMapping("/{id}/rooms/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Integer> generate(@PathVariable Long id, @Valid @RequestBody InventoryDtos.GenerateRoomsRequest request) {
        return Map.of("roomsCreated", generationService.generate(id, request));
    }

    @GetMapping("/{id}/rooms")
    public List<InventoryDtos.RoomResponse> rooms(@PathVariable Long id, @RequestParam(required = false) Integer floor) {
        access.requireCanView(CurrentUser.get(), id);
        return queryService.map(id, floor);
    }

    @GetMapping
    public List<InventoryDtos.DormitoryResponse> list() {
        return dormitoryService.list(CurrentUser.get());
    }

    @GetMapping("/{id}/rooms/{roomId}/beds")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<InventoryDtos.BedResponse> beds(@PathVariable Long id, @PathVariable Long roomId) {
        access.requireManagerOf(CurrentUser.get(), id);
        return queryService.beds(id, roomId);
    }
}
