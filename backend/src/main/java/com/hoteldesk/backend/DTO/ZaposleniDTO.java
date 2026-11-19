package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.Pol;
import com.hoteldesk.backend.model.TipZaposlenog;
import com.hoteldesk.backend.model.Zaposleni;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ZaposleniDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String ime;

    @NotBlank
    private String prezime;

    @NotBlank
    @Email
    private String email;

    @JsonProperty(access = WRITE_ONLY)
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    private String password;

    private String brojTelefona;

    private Pol pol;

    @NotNull
    private TipZaposlenog tipZaposlenog;

    @NotNull
    private Long hotelId;

    @JsonProperty(access = READ_ONLY)
    private String hotelNaziv;

    public static ZaposleniDTO from(Zaposleni z) {
        return new ZaposleniDTO(z.getId(), z.getIme(), z.getPrezime(), z.getEmail(), null,
                z.getBrojTelefona(), z.getPol(), z.getTipZaposlenog(),
                z.getHotel().getId(), z.getHotel().getNaziv());
    }
}
