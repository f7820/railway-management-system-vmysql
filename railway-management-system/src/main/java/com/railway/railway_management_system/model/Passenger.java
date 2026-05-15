package com.railway.railway_management_system.model;

import com.railway.railway_management_system.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name = "passengers")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Passenger {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer identificationNumber;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;


    public Passenger(Integer identificationNumber, String firstName, String lastName, Gender gender, String email, String phoneNumber) {
        this.identificationNumber = identificationNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
