package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tiketi")
@Getter
@Setter
@NoArgsConstructor
public class Tiket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipTiketa tipTiketa;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String sadrzaj;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTiketa status = StatusTiketa.OTVOREN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    private LocalDateTime datumZatvaranja;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne
    @JoinColumn(name = "soba_id")
    private Soba soba;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kreirao_id")
    private Korisnik kreirao;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dodeljen_id")
    private Zaposleni dodeljen;

    @PrePersist
    void prePersist() {
        if (datumKreiranja == null) {
            datumKreiranja = LocalDateTime.now();
        }
    }
}
