package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.ZaposleniDTO;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.ZaposleniService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ZaposleniController {

    private final ZaposleniService zaposleniService;
    private final TrenutniKorisnik trenutniKorisnik;

    @GetMapping("/hoteli/{hotelId}/zaposleni")
    public List<ZaposleniDTO> zaposleniHotela(@PathVariable Long hotelId) {
        return zaposleniService.findByHotel(hotelId, trenutniKorisnik.getId());
    }

    @GetMapping("/zaposleni/{id}")
    public ZaposleniDTO getById(@PathVariable Long id) {
        return zaposleniService.getById(id, trenutniKorisnik.getId());
    }

    @PostMapping("/zaposleni")
    @ResponseStatus(HttpStatus.CREATED)
    public ZaposleniDTO kreiraj(@Valid @RequestBody ZaposleniDTO dto) {
        return zaposleniService.kreiraj(dto, trenutniKorisnik.getId());
    }

    @PutMapping("/zaposleni/{id}")
    public ZaposleniDTO izmeni(@PathVariable Long id, @Valid @RequestBody ZaposleniDTO dto) {
        return zaposleniService.izmeni(id, dto, trenutniKorisnik.getId());
    }

    @DeleteMapping("/zaposleni/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable Long id) {
        zaposleniService.obrisi(id, trenutniKorisnik.getId());
    }
}
