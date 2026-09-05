package br.com.fiap.equipment_rent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "rent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode

public class Rent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teacher;

    @Column(nullable = false)
    private String course;

    @Column(nullable = false)
    private int classroom;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime takenTime;

    @Column(nullable = false)
    private LocalTime returnTime;

    @Column(nullable = false)
    private Equipment equipment;
}
