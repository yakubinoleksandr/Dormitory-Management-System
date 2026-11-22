package ua.kpi.dormitory.inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dormitory_id")
    private Dormitory dormitory;

    @Column(nullable = false)
    private int floor;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    /** Блок задається лише для типу BLOCK; для CORRIDOR залишається null. */
    @Column(name = "block_name", length = 20)
    private String blockName;

    @Column(nullable = false)
    private int capacity = 4;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.FREE;
}
