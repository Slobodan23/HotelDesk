package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.Hotel;
import com.hoteldesk.backend.model.Vlasnik;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotelDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String naziv;

    @Size(max = 2000)
    private String opis;

    @Min(1)
    @Max(5)
    private Integer brojZvezdica;

    @Email
    private String email;

    private String brojTelefona;

    @NotNull
    @Valid
    private AdresaDTO adresa;

    private Long vlasnikId;

    @JsonProperty(access = READ_ONLY)
    private String vlasnikImePrezime;

    @JsonProperty(access = READ_ONLY)
    private Double prosecnaOcena;

    public static HotelDTO from(Hotel h) {
        return from(h, null);
    }

    public static HotelDTO from(Hotel h, Double prosecnaOcena) {
        Vlasnik v = h.getVlasnik();
        return new HotelDTO(h.getId(), h.getNaziv(), h.getOpis(), h.getBrojZvezdica(), h.getEmail(),
                h.getBrojTelefona(), AdresaDTO.from(h.getAdresa()),
                v.getId(), v.getIme() + " " + v.getPrezime(), prosecnaOcena);
    }
}
