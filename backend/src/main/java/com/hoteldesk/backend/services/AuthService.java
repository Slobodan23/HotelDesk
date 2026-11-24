package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.AuthResponse;
import com.hoteldesk.backend.DTO.KorisnikDTO;
import com.hoteldesk.backend.DTO.LoginRequest;
import com.hoteldesk.backend.exception.UnauthorizedException;
import com.hoteldesk.backend.model.Korisnik;
import com.hoteldesk.backend.repository.KorisnikRepository;
import com.hoteldesk.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final KorisnikRepository korisnikRepository;
    private final KorisnikService korisnikService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Korisnik korisnik = korisnikRepository.findByEmail(korisnikService.normalizujEmail(request.getEmail()))
                .filter(k -> passwordEncoder.matches(request.getPassword(), k.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Pogresan email ili lozinka"));
        return new AuthResponse(jwtService.generisiToken(korisnik), KorisnikDTO.from(korisnik));
    }
}
