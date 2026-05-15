package com.railway.railway_management_system.model;

import com.railway.railway_management_system.enums.BookingStatus;
import com.railway.railway_management_system.enums.SeatClass;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String bookingReference;        // e.g. 260316-A3BF

    @Column(nullable = false)
    private String seatNumber;             // e.g. E3, B10, S42

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Column(nullable = false)
    private Double finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatClass bookedClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column
    private LocalDateTime cancelDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @PrePersist
    public void onPrePersist() {
        String datePart   = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = UUID.randomUUID().toString().replace("-", "")
                               .substring(0, 8).toUpperCase();
        this.bookingReference = datePart + "-" + randomPart;
        this.bookingDate      = LocalDateTime.now();
        this.status           = BookingStatus.CONFIRMED; // ← moved here from @PostPersist
    }
}
