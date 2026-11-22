package ua.kpi.dormitory.repair;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Дозволені переходи життєвого циклу ремонтної заявки (діаграма станів, лаб. 2, розділ 7).
 * NEW → IN_PROGRESS | REJECTED; IN_PROGRESS → COMPLETED | REJECTED; COMPLETED і REJECTED — кінцеві.
 */
public final class RepairStatusPolicy {
    private static final Map<RepairStatus, Set<RepairStatus>> ALLOWED = new EnumMap<>(RepairStatus.class);

    static {
        ALLOWED.put(RepairStatus.NEW, EnumSet.of(RepairStatus.IN_PROGRESS, RepairStatus.REJECTED));
        ALLOWED.put(RepairStatus.IN_PROGRESS, EnumSet.of(RepairStatus.COMPLETED, RepairStatus.REJECTED));
        ALLOWED.put(RepairStatus.COMPLETED, EnumSet.noneOf(RepairStatus.class));
        ALLOWED.put(RepairStatus.REJECTED, EnumSet.noneOf(RepairStatus.class));
    }

    private RepairStatusPolicy() { }

    public static boolean canTransition(RepairStatus from, RepairStatus to) {
        return ALLOWED.get(from).contains(to);
    }

    /** Для відхилення потрібен непорожній коментар завідуючої. */
    public static boolean commentRequired(RepairStatus to) {
        return to == RepairStatus.REJECTED;
    }
}
