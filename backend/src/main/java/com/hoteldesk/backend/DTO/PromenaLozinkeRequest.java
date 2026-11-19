package com.hoteldesk.backend.DTO;

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
public class PromenaLozinkeRequest {

    @NotBlank
    private String staraLozinka;

    @NotBlank
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    private String novaLozinka;
}
