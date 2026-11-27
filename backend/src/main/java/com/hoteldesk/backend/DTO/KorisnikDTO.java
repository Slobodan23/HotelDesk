package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.Hibernate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KorisnikDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotBlank
    private String ime;

    @NotBlank
    private String prezime;

    @JsonProperty(access = READ_ONLY)
    @Email
    private String email;

    private String brojTelefona;

    private Pol pol;

    @JsonProperty(access = READ_ONLY)
    private Uloga uloga;

    @JsonProperty(access = READ_ONLY)
    private TipZaposlenog tipZaposlenog;

    @JsonProperty(access = READ_ONLY)
    private Long hotelId;

    public static KorisnikDTO from(Korisnik k) {
        if (k == null) {
            return null;
        }
        Zaposleni z = Hibernate.unproxy(k) instanceof Zaposleni zap ? zap : null;
        return new KorisnikDTO(k.getId(), k.getIme(), k.getPrezime(), k.getEmail(),
                k.getBrojTelefona(), k.getPol(), ulogaOf(k),
                z != null ? z.getTipZaposlenog() : null,
                z != null ? z.getHotel().getId() : null);
    }

    public static Uloga ulogaOf(Korisnik k) {
        Object stvarni = Hibernate.unproxy(k);
        return switch (stvarni) {
            case Admin a -> Uloga.ADMIN;
            case Vlasnik v -> Uloga.VLASNIK;
            case Zaposleni z -> Uloga.ZAPOSLENI;
            case Gost g -> Uloga.GOST;
            default -> throw new IllegalStateException("Nepoznat tip korisnika: " + stvarni.getClass());
        };
    }
}
