package ua.kpi.dormitory.profile;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.dormitory.account.Account;
import ua.kpi.dormitory.inventory.Dormitory;

import java.time.LocalDateTime;

/** Зв'язок N:M «завідуюча — гуртожиток» (FR-03). */
@Entity
@Table(name = "dormitory_managers")
@Getter @Setter @NoArgsConstructor
public class DormitoryManager {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dormitory_id")
    private Dormitory dormitory;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
