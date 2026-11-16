package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recenzije")
@Getter
@Setter
@NoArgsConstructor
public class Recenzija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer ocena;

    @Column(length = 2000)
    private String tekstRecenzije;

    @Column(nullable = false, updatable = false)
    private LocalDateTime datum;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gost_id", nullable = false)
    private Gost gost;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rezervacija_id", unique = true)
    private Rezervacija rezervacija;

    @PrePersist
    void prePersist() {
        if (datum == null) {
            datum = LocalDateTime.now();
        }
    }
}
