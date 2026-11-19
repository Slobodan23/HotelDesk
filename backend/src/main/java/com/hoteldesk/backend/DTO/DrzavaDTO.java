package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.Drzava;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrzavaDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String naziv;

    public static DrzavaDTO from(Drzava d) {
        return new DrzavaDTO(d.getId(), d.getNaziv());
    }
}
