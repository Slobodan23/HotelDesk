package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.DrzavaDTO;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.Drzava;
import com.hoteldesk.backend.repository.DrzavaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrzavaService {

    private final DrzavaRepository drzavaRepository;

    public Drzava getDrzava(Long id) {
        return drzavaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Drzava nije pronadjena"));
    }

    public List<DrzavaDTO> findAll() {
        return drzavaRepository.findAllByOrderByNazivAsc().stream().map(DrzavaDTO::from).toList();
    }

    public DrzavaDTO getById(Long id) {
        return DrzavaDTO.from(getDrzava(id));
    }

    @Transactional
    public DrzavaDTO kreiraj(DrzavaDTO dto) {
        String naziv = dto.getNaziv().trim();
        if (drzavaRepository.existsByNazivIgnoreCase(naziv)) {
            throw new ConflictException("Drzava sa ovim nazivom vec postoji");
        }
        Drzava drzava = new Drzava();
        drzava.setNaziv(naziv);
        return DrzavaDTO.from(drzavaRepository.save(drzava));
    }

    @Transactional
    public DrzavaDTO izmeni(Long id, DrzavaDTO dto) {
        Drzava drzava = getDrzava(id);
        String naziv = dto.getNaziv().trim();
        if (!drzava.getNaziv().equalsIgnoreCase(naziv) && drzavaRepository.existsByNazivIgnoreCase(naziv)) {
            throw new ConflictException("Drzava sa ovim nazivom vec postoji");
        }
        drzava.setNaziv(naziv);
        return DrzavaDTO.from(drzava);
    }

    @Transactional
    public void obrisi(Long id) {
        drzavaRepository.delete(getDrzava(id));
        drzavaRepository.flush();
    }
}
