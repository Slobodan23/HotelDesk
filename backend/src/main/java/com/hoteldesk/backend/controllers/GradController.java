package com.hoteldesk.backend.controllers;

import com.hoteldesk.backend.DTO.GradDTO;
import com.hoteldesk.backend.services.GradService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gradovi")
@RequiredArgsConstructor
public class GradController {

    private final GradService gradService;

    @GetMapping
    public List<GradDTO> findAll() {
        return gradService.findAll();
    }

    @GetMapping("/{id}")
    public GradDTO getById(@PathVariable Long id) {
        return gradService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public GradDTO kreiraj(@Valid @RequestBody GradDTO dto) {
        return gradService.kreiraj(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GradDTO izmeni(@PathVariable Long id, @Valid @RequestBody GradDTO dto) {
        return gradService.izmeni(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void obrisi(@PathVariable Long id) {
        gradService.obrisi(id);
    }
}
