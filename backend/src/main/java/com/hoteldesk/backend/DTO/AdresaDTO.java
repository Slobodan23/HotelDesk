package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.Adresa;
import com.hoteldesk.backend.model.Grad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdresaDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String ulica;

    @NotBlank
    private String broj;

    @NotNull
    private Long gradId;

    @JsonProperty(access = READ_ONLY)
    private String gradNaziv;

    @JsonProperty(access = READ_ONLY)
    private String postanskiBroj;

    @JsonProperty(access = READ_ONLY)
    private String drzavaNaziv;

    public static AdresaDTO from(Adresa a) {
        Grad g = a.getGrad();
        return new AdresaDTO(a.getId(), a.getUlica(), a.getBroj(),
                g.getId(), g.getNaziv(), g.getPostanskiBroj(), g.getDrzava().getNaziv());
    }
}
