package com.railway.railway_management_system.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String routeNumber; // i.e(RT001, RT009)

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false)
    private String startStation;

    @Column(nullable = false)
    private String endStation;

    @Column(nullable = false)
    private Double distanceKm;

    @Column(nullable = false)
    private Integer stops;

    @PostPersist
    public void generateRouteNumber() {
        this.routeNumber = String.format("RT%03d" , this.id);
    }
}
