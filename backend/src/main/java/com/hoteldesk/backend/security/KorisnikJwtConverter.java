package com.hoteldesk.backend.security;

import com.hoteldesk.backend.DTO.KorisnikDTO;
import com.hoteldesk.backend.model.Korisnik;
import com.hoteldesk.backend.repository.KorisnikRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KorisnikJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KorisnikRepository korisnikRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Korisnik korisnik = korisnikRepository.findById(Long.valueOf(jwt.getSubject()))
                .filter(k -> !k.isObrisan())
                .orElseThrow(() -> new InvalidBearerTokenException("Nalog ne postoji ili je obrisan"));
        String uloga = "ROLE_" + KorisnikDTO.ulogaOf(korisnik).name();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(uloga)), jwt.getSubject());
    }
}
