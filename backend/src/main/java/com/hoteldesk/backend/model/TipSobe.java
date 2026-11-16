package com.hoteldesk.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipovi_soba")
@Getter
@Setter
@NoArgsConstructor
public class TipSobe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String tip;

    @Min(1)
    @Column(nullable = false)
    private int maxGostiju;

    @Column(length = 1000)
    private String opis;
}
