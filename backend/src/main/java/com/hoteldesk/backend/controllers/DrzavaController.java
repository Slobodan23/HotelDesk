package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.DrzavaDTO;
import com.hoteldesk.backend.DTO.GradDTO;
import com.hoteldesk.backend.services.DrzavaService;
import com.hoteldesk.backend.services.GradService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drzave")
@RequiredArgsConstructor
public class DrzavaController {

    private final DrzavaService drzavaService;
    private final GradService gradService;

    @GetMapping
    public List<DrzavaDTO> findAll() {
        return drzavaService.findAll();
    }

    @GetMapping("/{id}")
    public DrzavaDTO getById(@PathVariable Long id) {
        return drzavaService.getById(id);
    }

    @GetMapping("/{id}/gradovi")
    public List<GradDTO> gradovi(@PathVariable Long id) {
        return gradService.findByDrzava(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DrzavaDTO kreiraj(@Valid @RequestBody DrzavaDTO dto) {
        return drzavaService.kreiraj(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DrzavaDTO izmeni(@PathVariable Long id, @Valid @RequestBody DrzavaDTO dto) {
        return drzavaService.izmeni(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void obrisi(@PathVariable Long id) {
        drzavaService.obrisi(id);
    }
}
