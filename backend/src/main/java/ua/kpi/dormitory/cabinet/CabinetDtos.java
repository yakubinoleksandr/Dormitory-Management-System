package ua.kpi.dormitory.cabinet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CabinetDtos {
    private CabinetDtos() { }

    public record Placement(Long settlementId, String contractNumber, LocalDate startDate, LocalDate endDate,
                            Long dormitoryId, String dormitoryName, int floor, String blockName,
                            String roomNumber, Long roomId, int bedNumber) { }

    /**
     * Особистий кабінет мешканця. Фінансові дані: сума підтверджених оплат; точний борг не обчислюється,
     * бо в схемі БД немає тарифів і нарахувань (лаб. 2, п. 10.2).
     */
    public record CabinetResponse(String fullName, Integer course, String groupName, Placement placement,
                                  BigDecimal verifiedPaymentsTotal, List<String> neighbours) { }
}
