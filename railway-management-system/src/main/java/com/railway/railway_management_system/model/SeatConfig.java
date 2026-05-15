package com.railway.railway_management_system.model;

import com.railway.railway_management_system.enums.SeatClass;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class SeatConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatClass seatClass;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Double basePrice;

    @ManyToOne
    @JoinColumn(name = "train_id",nullable = false)
    private Train train;



}