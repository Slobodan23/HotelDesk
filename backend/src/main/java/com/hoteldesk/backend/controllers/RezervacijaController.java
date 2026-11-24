package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.RezervacijaDTO;
import com.hoteldesk.backend.DTO.RezervacijaStatusRequest;
import com.hoteldesk.backend.model.StatusRezervacije;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.RezervacijaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RezervacijaController {

    private final RezervacijaService rezervacijaService;
    private final TrenutniKorisnik trenutniKorisnik;

    @PostMapping("/rezervacije")
    @ResponseStatus(HttpStatus.CREATED)
    public RezervacijaDTO kreiraj(@Valid @RequestBody RezervacijaDTO dto) {
        return rezervacijaService.kreiraj(dto, trenutniKorisnik.getId());
    }

    @GetMapping("/rezervacije/moje")
    public List<RezervacijaDTO> mojeRezervacije() {
        return rezervacijaService.mojeRezervacije(trenutniKorisnik.getId());
    }

    @GetMapping("/rezervacije/{id}")
    public RezervacijaDTO getById(@PathVariable Long id) {
        return rezervacijaService.getById(id, trenutniKorisnik.getId());
    }

    @PatchMapping("/rezervacije/{id}/otkazi")
    public RezervacijaDTO otkazi(@PathVariable Long id) {
        return rezervacijaService.otkazi(id, trenutniKorisnik.getId());
    }

    @PatchMapping("/rezervacije/{id}/status")
    public RezervacijaDTO promeniStatus(@PathVariable Long id, @Valid @RequestBody RezervacijaStatusRequest request) {
        return rezervacijaService.promeniStatus(id, request.getStatus(), trenutniKorisnik.getId());
    }

    @GetMapping("/hoteli/{hotelId}/rezervacije")
    public List<RezervacijaDTO> rezervacijeHotela(@PathVariable Long hotelId,
                                                  @RequestParam(required = false) StatusRezervacije status) {
        return rezervacijaService.rezervacijeHotela(hotelId, status, trenutniKorisnik.getId());
    }

    @GetMapping("/hoteli/{hotelId}/dolasci")
    public List<RezervacijaDTO> dolasci(@PathVariable Long hotelId,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum) {
        return rezervacijaService.dolasci(hotelId, datum != null ? datum : LocalDate.now(), trenutniKorisnik.getId());
    }

    @GetMapping("/hoteli/{hotelId}/odlasci")
    public List<RezervacijaDTO> odlasci(@PathVariable Long hotelId,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum) {
        return rezervacijaService.odlasci(hotelId, datum != null ? datum : LocalDate.now(), trenutniKorisnik.getId());
    }
}
