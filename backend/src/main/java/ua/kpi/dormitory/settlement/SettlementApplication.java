package ua.kpi.dormitory.settlement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.dormitory.account.Account;
import ua.kpi.dormitory.inventory.Dormitory;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_applications")
@Getter @Setter @NoArgsConstructor
public class SettlementApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_account_id")
    private Account residentAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dormitory_id")
    private Dormitory dormitory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
