package ua.kpi.dormitory.repair;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepairStatusPolicyTest {
    @Test
    void newCanStartOrBeRejected() {
        assertTrue(RepairStatusPolicy.canTransition(RepairStatus.NEW, RepairStatus.IN_PROGRESS));
        assertTrue(RepairStatusPolicy.canTransition(RepairStatus.NEW, RepairStatus.REJECTED));
    }

    @Test
    void newCannotJumpToCompleted() {
        assertFalse(RepairStatusPolicy.canTransition(RepairStatus.NEW, RepairStatus.COMPLETED));
    }

    @Test
    void finalStatesHaveNoTransitions() {
        for (RepairStatus to : RepairStatus.values()) {
            assertFalse(RepairStatusPolicy.canTransition(RepairStatus.COMPLETED, to));
            assertFalse(RepairStatusPolicy.canTransition(RepairStatus.REJECTED, to));
        }
    }

    @Test
    void rejectionRequiresComment() {
        assertTrue(RepairStatusPolicy.commentRequired(RepairStatus.REJECTED));
        assertFalse(RepairStatusPolicy.commentRequired(RepairStatus.COMPLETED));
    }
}
