package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "placanja")
@Getter
@Setter
@NoArgsConstructor
public class Placanje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal iznos;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NacinPlacanja nacinPlacanja;

    @Column(nullable = false)
    private boolean placeno = false;

    private LocalDateTime datumPlacanja;

    @ManyToOne
    @JoinColumn(name = "kartica_id")
    private Kartica kartica;

    @JsonIgnore
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rezervacija_id", nullable = false, unique = true)
    private Rezervacija rezervacija;
}
