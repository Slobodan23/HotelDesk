package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.TipSobe;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class TipSobeDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String tip;

    @Min(1)
    private int maxGostiju;

    @Size(max = 1000)
    private String opis;

    public static TipSobeDTO from(TipSobe t) {
        return new TipSobeDTO(t.getId(), t.getTip(), t.getMaxGostiju(), t.getOpis());
    }
}
