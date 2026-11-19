package com.hoteldesk.backend.DTO;

import com.hoteldesk.backend.model.Pol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistracijaRequest {

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
}
