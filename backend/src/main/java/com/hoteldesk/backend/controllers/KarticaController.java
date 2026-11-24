package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.KarticaDTO;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.KarticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kartice")
@RequiredArgsConstructor
public class KarticaController {

    private final KarticaService karticaService;
    private final TrenutniKorisnik trenutniKorisnik;

    @GetMapping
    public List<KarticaDTO> mojeKartice() {
        return karticaService.mojeKartice(trenutniKorisnik.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KarticaDTO dodaj(@Valid @RequestBody KarticaDTO dto) {
        return karticaService.dodaj(dto, trenutniKorisnik.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable Long id) {
        karticaService.obrisi(id, trenutniKorisnik.getId());
    }
}
