package com.hoteldesk.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "adrese")
@Getter
@Setter
@NoArgsConstructor
public class Adresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String ulica;

    @NotBlank
    @Column(nullable = false)
    private String broj;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "grad_id", nullable = false)
    private Grad grad;
}
