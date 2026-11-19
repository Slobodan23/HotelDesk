package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.Gost;
import com.hoteldesk.backend.model.Recenzija;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecenzijaDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotNull
    private Long rezervacijaId;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer ocena;

    @Size(max = 2000)
    private String tekstRecenzije;

    @JsonProperty(access = READ_ONLY)
    private LocalDateTime datum;

    @JsonProperty(access = READ_ONLY)
    private Long gostId;

    @JsonProperty(access = READ_ONLY)
    private String gostImePrezime;

    @JsonProperty(access = READ_ONLY)
    private Long hotelId;

    public static RecenzijaDTO from(Recenzija r) {
        Gost g = r.getGost();
        return new RecenzijaDTO(r.getId(),
                r.getRezervacija() != null ? r.getRezervacija().getId() : null,
                r.getOcena(), r.getTekstRecenzije(), r.getDatum(),
                g.getId(), g.getIme() + " " + g.getPrezime(), r.getHotel().getId());
    }
}
