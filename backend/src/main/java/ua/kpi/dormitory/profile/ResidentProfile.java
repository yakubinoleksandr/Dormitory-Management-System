package ua.kpi.dormitory.profile;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.dormitory.account.Account;

@Entity
@Table(name = "resident_profiles")
@Getter @Setter @NoArgsConstructor
public class ResidentProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    private Integer course;

    @Column(name = "group_name", length = 20)
    private String groupName;

    @Column(length = 20)
    private String phone;

    @Column(name = "benefits_info")
    private String benefitsInfo;
}
