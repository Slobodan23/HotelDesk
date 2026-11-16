package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hoteli")
@Getter
@Setter
@NoArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String naziv;

    @Column(length = 2000)
    private String opis;

    @Min(1)
    @Max(5)
    private Integer brojZvezdica;

    private String email;

    private String brojTelefona;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "vlasnik_id", nullable = false)
    private Vlasnik vlasnik;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "adresa_id", nullable = false, unique = true)
    private Adresa adresa;

    @JsonIgnore
    @OneToMany(mappedBy = "hotel")
    private List<Zaposleni> zaposleni = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Soba> sobe = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "hotel")
    private List<Recenzija> recenzije = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "hotel")
    private List<Tiket> tiketi = new ArrayList<>();
}
