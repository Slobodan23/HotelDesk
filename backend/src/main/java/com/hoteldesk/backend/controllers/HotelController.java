package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.HotelDTO;
import com.hoteldesk.backend.security.TrenutniKorisnik;
import com.hoteldesk.backend.services.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoteli")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final TrenutniKorisnik trenutniKorisnik;

    @GetMapping
    public List<HotelDTO> pretraga(@RequestParam(required = false) Long gradId,
                                   @RequestParam(required = false) String naziv,
                                   @RequestParam(required = false) Integer minZvezdica) {
        return hotelService.pretraga(gradId, naziv, minZvezdica);
    }

    @GetMapping("/moji")
    public List<HotelDTO> mojiHoteli() {
        return hotelService.findByVlasnik(trenutniKorisnik.getId());
    }

    @GetMapping("/{id}")
    public HotelDTO getById(@PathVariable Long id) {
        return hotelService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelDTO kreiraj(@Valid @RequestBody HotelDTO dto) {
        return hotelService.kreiraj(dto, trenutniKorisnik.getId());
    }

    @PutMapping("/{id}")
    public HotelDTO izmeni(@PathVariable Long id, @Valid @RequestBody HotelDTO dto) {
        return hotelService.izmeni(id, dto, trenutniKorisnik.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable Long id) {
        hotelService.obrisi(id, trenutniKorisnik.getId());
    }
}
