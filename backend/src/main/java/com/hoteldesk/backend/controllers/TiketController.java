package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.TiketDTO;
import com.hoteldesk.backend.DTO.TiketUpdateRequest;
import com.hoteldesk.backend.model.StatusTiketa;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.TiketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TiketController {

    private final TiketService tiketService;
    private final TrenutniKorisnik trenutniKorisnik;

    @PostMapping("/tiketi")
    @ResponseStatus(HttpStatus.CREATED)
    public TiketDTO kreiraj(@Valid @RequestBody TiketDTO dto) {
        return tiketService.kreiraj(dto, trenutniKorisnik.getId());
    }

    @GetMapping("/tiketi/moji")
    public List<TiketDTO> mojiTiketi() {
        return tiketService.mojiTiketi(trenutniKorisnik.getId());
    }

    @GetMapping("/tiketi/dodeljeni")
    public List<TiketDTO> dodeljeniTiketi() {
        return tiketService.dodeljeniTiketi(trenutniKorisnik.getId());
    }

    @GetMapping("/tiketi/{id}")
    public TiketDTO getById(@PathVariable Long id) {
        return tiketService.getById(id, trenutniKorisnik.getId());
    }

    @PatchMapping("/tiketi/{id}")
    public TiketDTO azuriraj(@PathVariable Long id, @RequestBody TiketUpdateRequest request) {
        return tiketService.azuriraj(id, request, trenutniKorisnik.getId());
    }

    @GetMapping("/hoteli/{hotelId}/tiketi")
    public List<TiketDTO> tiketiHotela(@PathVariable Long hotelId,
                                       @RequestParam(required = false) StatusTiketa status) {
        return tiketService.findByHotel(hotelId, status, trenutniKorisnik.getId());
    }
}
