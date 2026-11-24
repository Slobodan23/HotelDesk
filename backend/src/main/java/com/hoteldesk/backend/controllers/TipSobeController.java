package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.TipSobeDTO;
import com.hoteldesk.backend.services.TipSobeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipovi-soba")
@RequiredArgsConstructor
public class TipSobeController {

    private final TipSobeService tipSobeService;

    @GetMapping
    public List<TipSobeDTO> findAll() {
        return tipSobeService.findAll();
    }

    @GetMapping("/{id}")
    public TipSobeDTO getById(@PathVariable Long id) {
        return tipSobeService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TipSobeDTO kreiraj(@Valid @RequestBody TipSobeDTO dto) {
        return tipSobeService.kreiraj(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TipSobeDTO izmeni(@PathVariable Long id, @Valid @RequestBody TipSobeDTO dto) {
        return tipSobeService.izmeni(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void obrisi(@PathVariable Long id) {
        tipSobeService.obrisi(id);
    }
}
