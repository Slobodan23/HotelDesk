package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.KorisnikDTO;
import com.hoteldesk.backend.DTO.KreiranjeKorisnikaRequest;
import com.hoteldesk.backend.DTO.IzmenaKorisnikaRequest;
import com.hoteldesk.backend.DTO.PromenaLozinkeRequest;
import com.hoteldesk.backend.DTO.RegistracijaRequest;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.KorisnikService;
import com.hoteldesk.backend.services.UlogaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/korisnici")
@RequiredArgsConstructor
public class KorisnikController {

    private final KorisnikService korisnikService;
    private final UlogaService ulogaService;
    private final TrenutniKorisnik trenutniKorisnik;

    @GetMapping("/me")
    public KorisnikDTO mojProfil() {
        return korisnikService.getById(trenutniKorisnik.getId());
    }

    @PutMapping("/me")
    public KorisnikDTO izmeniProfil(@Valid @RequestBody KorisnikDTO dto) {
        return korisnikService.izmeniProfil(trenutniKorisnik.getId(), dto);
    }

    @PutMapping("/me/lozinka")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void promeniLozinku(@Valid @RequestBody PromenaLozinkeRequest request) {
        korisnikService.promeniLozinku(trenutniKorisnik.getId(), request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<KorisnikDTO> findAll() {
        return korisnikService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public KorisnikDTO getById(@PathVariable Long id) {
        return korisnikService.getById(id);
    }

    @PostMapping("/vlasnici")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public KorisnikDTO kreirajVlasnika(@Valid @RequestBody RegistracijaRequest request) {
        return korisnikService.registrujVlasnika(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public KorisnikDTO kreiraj(@Valid @RequestBody KreiranjeKorisnikaRequest request) {
        return ulogaService.kreiraj(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public KorisnikDTO izmeni(@PathVariable Long id, @Valid @RequestBody IzmenaKorisnikaRequest request) {
        return ulogaService.izmeni(id, request, trenutniKorisnik.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void obrisi(@PathVariable Long id) {
        ulogaService.obrisi(id, trenutniKorisnik.getId());
    }
}
