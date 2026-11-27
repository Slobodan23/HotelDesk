package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.NoveSobeRequest;
import com.hoteldesk.backend.DTO.SobaDTO;
import com.hoteldesk.backend.DTO.SobaStatusRequest;
import com.hoteldesk.backend.model.StatusSobe;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.SobaService;
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
public class SobaController {

    private final SobaService sobaService;
    private final TrenutniKorisnik trenutniKorisnik;

    @GetMapping("/hoteli/{hotelId}/sobe")
    public List<SobaDTO> sobeHotela(@PathVariable Long hotelId,
                                    @RequestParam(required = false) StatusSobe status) {
        if (status == null) {
            return sobaService.findByHotel(hotelId);
        }
        return sobaService.findByHotelAndStatus(hotelId, status, trenutniKorisnik.getId());
    }

    @GetMapping("/hoteli/{hotelId}/sobe/slobodne")
    public List<SobaDTO> slobodneSobe(@PathVariable Long hotelId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumDolaska,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumOdlaska,
                                      @RequestParam(defaultValue = "1") int brojGostiju) {
        return sobaService.slobodneSobe(hotelId, datumDolaska, datumOdlaska, brojGostiju);
    }

    @GetMapping("/sobe/{id}")
    public SobaDTO getById(@PathVariable Long id) {
        return sobaService.getById(id);
    }

    @PostMapping("/sobe")
    @ResponseStatus(HttpStatus.CREATED)
    public SobaDTO kreiraj(@Valid @RequestBody SobaDTO dto) {
        return sobaService.kreiraj(dto, trenutniKorisnik.getId());
    }

    @PostMapping("/hoteli/{hotelId}/sobe")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SobaDTO> kreirajSve(@PathVariable Long hotelId, @Valid @RequestBody NoveSobeRequest request) {
        return sobaService.kreirajSve(hotelId, request.getSobe(), trenutniKorisnik.getId());
    }

    @PutMapping("/sobe/{id}")
    public SobaDTO izmeni(@PathVariable Long id, @Valid @RequestBody SobaDTO dto) {
        return sobaService.izmeni(id, dto, trenutniKorisnik.getId());
    }

    @PatchMapping("/sobe/{id}/status")
    public SobaDTO promeniStatus(@PathVariable Long id, @Valid @RequestBody SobaStatusRequest request) {
        return sobaService.promeniStatus(id, request.getStatus(), trenutniKorisnik.getId());
    }

    @DeleteMapping("/sobe/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable Long id) {
        sobaService.obrisi(id, trenutniKorisnik.getId());
    }
}
