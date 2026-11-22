package ua.kpi.dormitory.repair;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.dormitory.account.Account;
import ua.kpi.dormitory.inventory.Room;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_requests")
@Getter @Setter @NoArgsConstructor
public class RepairRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_account_id")
    private Account residentAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepairCategory category;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepairStatus status = RepairStatus.NEW;

    @Column(name = "manager_comment")
    private String managerComment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
