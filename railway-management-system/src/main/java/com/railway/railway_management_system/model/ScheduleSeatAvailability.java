package com.railway.railway_management_system.model;

import com.railway.railway_management_system.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "schedule_seat_availability",
    uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "seat_class"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ScheduleSeatAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    /** Copied from SeatConfig at schedule-creation time. Never changes. */
    @Column(nullable = false)
    private Integer totalSeats;

    /** Decremented on booking, incremented on cancellation. */
    @Column(nullable = false)
    private Integer availableSeats;

    /**
     * Monotonically increasing — always points to the next seat number to assign.
     * Never decremented, so seat numbers are never reused (no duplicate IDs after cancellations).
     */
    @Column(nullable = false)
    private Integer nextSeatNumber;

    /** Copied from SeatConfig. Could be adjusted per schedule if needed. */
    @Column(nullable = false)
    private Double basePrice;
}
