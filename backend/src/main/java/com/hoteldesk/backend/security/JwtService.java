package com.hoteldesk.backend.security;

import com.hoteldesk.backend.DTO.KorisnikDTO;
import com.hoteldesk.backend.model.Korisnik;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    public static final String CLAIM_ULOGA = "uloga";

    private final JwtEncoder jwtEncoder;
    private final long trajanjeMinuta;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.jwt.trajanje-minuta}") long trajanjeMinuta) {
        this.jwtEncoder = jwtEncoder;
        this.trajanjeMinuta = trajanjeMinuta;
    }

    public String generisiToken(Korisnik korisnik) {
        Instant sada = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("hoteldesk")
                .subject(korisnik.getId().toString())
                .issuedAt(sada)
                .expiresAt(sada.plus(trajanjeMinuta, ChronoUnit.MINUTES))
                .claim("email", korisnik.getEmail())
                .claim(CLAIM_ULOGA, KorisnikDTO.ulogaOf(korisnik).name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
