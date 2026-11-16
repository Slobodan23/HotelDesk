package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zaposleni")
@Getter
@Setter
@NoArgsConstructor
public class Zaposleni extends Korisnik {

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipZaposlenog tipZaposlenog;
}
