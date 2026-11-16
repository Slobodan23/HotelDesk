package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rezervacije")
@Getter
@Setter
@NoArgsConstructor
public class Rezervacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate datumDolaska;

    @NotNull
    @Column(nullable = false)
    private LocalDate datumOdlaska;

    @Min(1)
    @Column(nullable = false)
    private int brojGostiju = 1;

    @Column(precision = 10, scale = 2)
    private BigDecimal ukupnaCena;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRezervacije status = StatusRezervacije.KREIRANA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gost_id", nullable = false)
    private Gost gost;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "soba_id", nullable = false)
    private Soba soba;

    @OneToOne(mappedBy = "rezervacija", cascade = CascadeType.ALL, orphanRemoval = true)
    private Placanje placanje;

    @PrePersist
    void prePersist() {
        if (datumKreiranja == null) {
            datumKreiranja = LocalDateTime.now();
        }
    }

    @JsonIgnore
    @AssertTrue(message = "Datum odlaska mora biti posle datuma dolaska")
    public boolean isPeriodIspravan() {
        return datumDolaska == null || datumOdlaska == null || datumOdlaska.isAfter(datumDolaska);
    }
}
