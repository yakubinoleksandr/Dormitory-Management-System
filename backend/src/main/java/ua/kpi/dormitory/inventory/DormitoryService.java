package ua.kpi.dormitory.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;

/** FR-01: створення гуртожитку з типом планування. */
@Service
public class DormitoryService {
    private final DormitoryRepository dormitories;

    public DormitoryService(DormitoryRepository dormitories) { this.dormitories = dormitories; }

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

    static InventoryDtos.DormitoryResponse toResponse(Dormitory d) {
        return new InventoryDtos.DormitoryResponse(d.getId(), d.getNameOrNumber(), d.getAddress(),
                d.getTotalFloors(), d.getLayoutType());
    }
}
