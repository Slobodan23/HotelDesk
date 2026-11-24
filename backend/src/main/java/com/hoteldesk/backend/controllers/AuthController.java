package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.AuthResponse;
import com.hoteldesk.backend.DTO.KorisnikDTO;
import com.hoteldesk.backend.DTO.LoginRequest;
import com.hoteldesk.backend.DTO.RegistracijaRequest;
import com.hoteldesk.backend.services.AuthService;
import com.hoteldesk.backend.services.KorisnikService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KorisnikService korisnikService;
    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registracija")
    @ResponseStatus(HttpStatus.CREATED)
    public KorisnikDTO registracija(@Valid @RequestBody RegistracijaRequest request) {
        return korisnikService.registrujGosta(request);
    }
}
