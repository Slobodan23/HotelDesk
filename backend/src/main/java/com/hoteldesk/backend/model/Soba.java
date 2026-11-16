package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sobe", uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_id", "broj_sobe"}))
@Getter
@Setter
@NoArgsConstructor
public class Soba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "broj_sobe", nullable = false)
    private Integer brojSobe;

    private Integer sprat;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cenaPoNoci;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSobe status = StatusSobe.CISTA;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "tip_sobe_id", nullable = false)
    private TipSobe tipSobe;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
}
