package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.RecenzijaDTO;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.RecenzijaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecenzijaController {

    private final RecenzijaService recenzijaService;
    private final TrenutniKorisnik trenutniKorisnik;

    @GetMapping("/hoteli/{hotelId}/recenzije")
    public List<RecenzijaDTO> recenzijeHotela(@PathVariable Long hotelId) {
        return recenzijaService.findByHotel(hotelId);
    }

    @GetMapping("/recenzije/moje")
    public List<RecenzijaDTO> mojeRecenzije() {
        return recenzijaService.mojeRecenzije(trenutniKorisnik.getId());
    }

    @PostMapping("/recenzije")
    @ResponseStatus(HttpStatus.CREATED)
    public RecenzijaDTO kreiraj(@Valid @RequestBody RecenzijaDTO dto) {
        return recenzijaService.kreiraj(dto, trenutniKorisnik.getId());
    }

    @DeleteMapping("/recenzije/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable Long id) {
        recenzijaService.obrisi(id, trenutniKorisnik.getId());
    }
}
