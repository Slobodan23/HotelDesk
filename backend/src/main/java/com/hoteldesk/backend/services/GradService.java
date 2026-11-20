package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.GradDTO;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.Drzava;
import com.hoteldesk.backend.model.Grad;
import com.hoteldesk.backend.repository.GradRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradService {

    private final GradRepository gradRepository;
    private final DrzavaService drzavaService;

    public Grad getGrad(Long id) {
        return gradRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grad nije pronadjen"));
    }

    public List<GradDTO> findAll() {
        return gradRepository.findAll().stream().map(GradDTO::from).toList();
    }

    public List<GradDTO> findByDrzava(Long drzavaId) {
        return gradRepository.findByDrzavaIdOrderByNazivAsc(drzavaId).stream().map(GradDTO::from).toList();
    }

    public GradDTO getById(Long id) {
        return GradDTO.from(getGrad(id));
    }

    @Transactional
    public GradDTO kreiraj(GradDTO dto) {
        Drzava drzava = drzavaService.getDrzava(dto.getDrzavaId());
        String naziv = dto.getNaziv().trim();
        if (gradRepository.existsByNazivIgnoreCaseAndDrzavaId(naziv, drzava.getId())) {
            throw new ConflictException("Grad sa ovim nazivom vec postoji u izabranoj drzavi");
        }
        Grad grad = new Grad();
        grad.setNaziv(naziv);
        grad.setPostanskiBroj(dto.getPostanskiBroj());
        grad.setDrzava(drzava);
        return GradDTO.from(gradRepository.save(grad));
    }

    @Transactional
    public GradDTO izmeni(Long id, GradDTO dto) {
        Grad grad = getGrad(id);
        Drzava drzava = drzavaService.getDrzava(dto.getDrzavaId());
        String naziv = dto.getNaziv().trim();
        boolean promenjen = !grad.getNaziv().equalsIgnoreCase(naziv) || !grad.getDrzava().getId().equals(drzava.getId());
        if (promenjen && gradRepository.existsByNazivIgnoreCaseAndDrzavaId(naziv, drzava.getId())) {
            throw new ConflictException("Grad sa ovim nazivom vec postoji u izabranoj drzavi");
        }
        grad.setNaziv(naziv);
        grad.setPostanskiBroj(dto.getPostanskiBroj());
        grad.setDrzava(drzava);
        return GradDTO.from(grad);
    }

    @Transactional
    public void obrisi(Long id) {
        gradRepository.delete(getGrad(id));
        gradRepository.flush();
    }
}
