package ua.kpi.dormitory.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.profile.DormitoryManagerRepository;
import ua.kpi.dormitory.security.AuthenticatedUser;

import java.util.List;

/** FR-01: створення гуртожитку з типом планування. */
@Service
public class DormitoryService {
    private final DormitoryRepository dormitories;
    private final DormitoryManagerRepository managers;

    public DormitoryService(DormitoryRepository dormitories, DormitoryManagerRepository managers) {
        this.dormitories = dormitories;
        this.managers = managers;
    }

    @Transactional
    public InventoryDtos.DormitoryResponse create(InventoryDtos.CreateDormitoryRequest req) {
        Dormitory d = new Dormitory();
        d.setNameOrNumber(req.nameOrNumber());
        d.setAddress(req.address());
        d.setTotalFloors(req.totalFloors());
        d.setLayoutType(req.layoutType());
        d = dormitories.save(d);
        return toResponse(d);
    }

    @Transactional(readOnly = true)
    public Dormitory getOrThrow(Long id) {
        return dormitories.findById(id).orElseThrow(() -> ApiException.notFound("Гуртожиток не знайдено"));
    }

    /**
     * Адміністратор і мешканець бачать список гуртожитків (мешканцю він потрібен для подання заяви);
     * завідуюча — лише закріплені за нею.
     */
    @Transactional(readOnly = true)
    public List<InventoryDtos.DormitoryResponse> list(AuthenticatedUser user) {
        List<Dormitory> all = user.isManager()
                ? dormitories.findAllById(managers.findDormitoryIdsByAccountId(user.id()))
                : dormitories.findAll();
        return all.stream().map(DormitoryService::toResponse).toList();
    }

    static InventoryDtos.DormitoryResponse toResponse(Dormitory d) {
        return new InventoryDtos.DormitoryResponse(d.getId(), d.getNameOrNumber(), d.getAddress(),
                d.getTotalFloors(), d.getLayoutType());
    }
}
