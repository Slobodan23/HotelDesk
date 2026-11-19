package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.Kartica;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KarticaDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String imeNosioca;

    @JsonProperty(access = WRITE_ONLY)
    @NotBlank
    @Pattern(regexp = "\\d{13,19}", message = "Neispravan broj kartice")
    private String brojKartice;

    @JsonProperty(access = READ_ONLY)
    private String poslednjeCetiriCifre;

    @Min(1)
    @Max(12)
    private int mesecIsteka;

    @Min(2000)
    private int godinaIsteka;

    public static KarticaDTO from(Kartica k) {
        if (k == null) {
            return null;
        }
        return new KarticaDTO(k.getId(), k.getImeNosioca(), null, k.getPoslednjeCetiriCifre(),
                k.getMesecIsteka(), k.getGodinaIsteka());
    }
}
