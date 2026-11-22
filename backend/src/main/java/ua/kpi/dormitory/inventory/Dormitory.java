package ua.kpi.dormitory.inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dormitories")
@Getter @Setter @NoArgsConstructor
public class Dormitory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_or_number", nullable = false, length = 100)
    private String nameOrNumber;

    private String address;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type", nullable = false, length = 20)
    private LayoutType layoutType;
}
