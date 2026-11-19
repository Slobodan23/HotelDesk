package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RezervacijaDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotNull
    private Long sobaId;

    @NotNull
    @FutureOrPresent
    private LocalDate datumDolaska;

    @NotNull
    private LocalDate datumOdlaska;

    @Min(1)
    private int brojGostiju;

    @JsonProperty(access = WRITE_ONLY)
    @NotNull
    private NacinPlacanja nacinPlacanja;

    @JsonProperty(access = WRITE_ONLY)
    private Long karticaId;

    @JsonProperty(access = READ_ONLY)
    private BigDecimal ukupnaCena;

    @JsonProperty(access = READ_ONLY)
    private StatusRezervacije status;

    @JsonProperty(access = READ_ONLY)
    private LocalDateTime datumKreiranja;

    @JsonProperty(access = READ_ONLY)
    private Long gostId;

    @JsonProperty(access = READ_ONLY)
    private String gostImePrezime;

    @JsonProperty(access = READ_ONLY)
    private Integer brojSobe;

    @JsonProperty(access = READ_ONLY)
    private Long hotelId;

    @JsonProperty(access = READ_ONLY)
    private String hotelNaziv;

    @JsonProperty(access = READ_ONLY)
    private PlacanjeDTO placanje;

    public static RezervacijaDTO from(Rezervacija r) {
        Gost g = r.getGost();
        Soba s = r.getSoba();
        Hotel h = s.getHotel();
        Placanje p = r.getPlacanje();
        return new RezervacijaDTO(r.getId(), s.getId(), r.getDatumDolaska(), r.getDatumOdlaska(),
                r.getBrojGostiju(), p != null ? p.getNacinPlacanja() : null, null,
                r.getUkupnaCena(), r.getStatus(), r.getDatumKreiranja(),
                g.getId(), g.getIme() + " " + g.getPrezime(),
                s.getBrojSobe(), h.getId(), h.getNaziv(), PlacanjeDTO.from(p));
    }

    @JsonIgnore
    @AssertTrue(message = "Datum odlaska mora biti posle datuma dolaska")
    public boolean isPeriodIspravan() {
        return datumDolaska == null || datumOdlaska == null || datumOdlaska.isAfter(datumDolaska);
    }

    @JsonIgnore
    @AssertTrue(message = "Za placanje karticom potrebno je izabrati karticu")
    public boolean isKarticaIzabrana() {
        return nacinPlacanja != NacinPlacanja.KARTICA || karticaId != null;
    }
}
