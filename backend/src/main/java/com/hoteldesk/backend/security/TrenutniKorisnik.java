package com.hoteldesk.backend.security;

import com.hoteldesk.backend.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TrenutniKorisnik {

    public Long getId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Long.valueOf(jwtAuth.getToken().getSubject());
        }
        throw new UnauthorizedException("Korisnik nije prijavljen");
    }
}
