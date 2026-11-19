package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class GradDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String naziv;

    private String postanskiBroj;

    @NotNull
    private Long drzavaId;

    @JsonProperty(access = READ_ONLY)
    private String drzavaNaziv;

    public static GradDTO from(Grad g) {
        return new GradDTO(g.getId(), g.getNaziv(), g.getPostanskiBroj(),
                g.getDrzava().getId(), g.getDrzava().getNaziv());
    }
}
