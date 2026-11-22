package ua.kpi.dormitory.inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "beds")
@Getter @Setter @NoArgsConstructor
public class Bed {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "bed_number", nullable = false)
    private int bedNumber;

    @Column(name = "is_occupied", nullable = false)
    private boolean occupied = false;
}
