package com.hoteldesk.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gradovi", uniqueConstraints = @UniqueConstraint(columnNames = {"naziv", "drzava_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Grad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String naziv;

    private String postanskiBroj;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "drzava_id", nullable = false)
    private Drzava drzava;
}
