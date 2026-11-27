package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hoteldesk.backend.model.Pol;
import com.hoteldesk.backend.model.TipZaposlenog;
import com.hoteldesk.backend.model.Uloga;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KreiranjeKorisnikaRequest {

    @NotBlank
    private String ime;

    @NotBlank
    private String prezime;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    private String password;

    private String brojTelefona;

    private Pol pol;

    @NotNull
    private Uloga uloga;

    private TipZaposlenog tipZaposlenog;

    private Long hotelId;

    @JsonIgnore
    @AssertTrue(message = "Za zaposlenog je potrebno izabrati posao i hotel")
    public boolean isZaposleniPotpun() {
        return uloga != Uloga.ZAPOSLENI || (tipZaposlenog != null && hotelId != null);
    }
}
