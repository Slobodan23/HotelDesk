package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.TipSobeDTO;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.TipSobe;
import com.hoteldesk.backend.repository.TipSobeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipSobeService {

    private final TipSobeRepository tipSobeRepository;

    public TipSobe getTipSobe(Long id) {
        return tipSobeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tip sobe nije pronadjen"));
    }

    public List<TipSobeDTO> findAll() {
        return tipSobeRepository.findAll().stream().map(TipSobeDTO::from).toList();
    }

    public TipSobeDTO getById(Long id) {
        return TipSobeDTO.from(getTipSobe(id));
    }

    @Transactional
    public TipSobeDTO kreiraj(TipSobeDTO dto) {
        String tip = dto.getTip().trim();
        if (tipSobeRepository.existsByTipIgnoreCase(tip)) {
            throw new ConflictException("Tip sobe sa ovim nazivom vec postoji");
        }
        TipSobe tipSobe = new TipSobe();
        popuni(tipSobe, tip, dto);
        return TipSobeDTO.from(tipSobeRepository.save(tipSobe));
    }

    @Transactional
    public TipSobeDTO izmeni(Long id, TipSobeDTO dto) {
        TipSobe tipSobe = getTipSobe(id);
        String tip = dto.getTip().trim();
        if (!tipSobe.getTip().equalsIgnoreCase(tip) && tipSobeRepository.existsByTipIgnoreCase(tip)) {
            throw new ConflictException("Tip sobe sa ovim nazivom vec postoji");
        }
        popuni(tipSobe, tip, dto);
        return TipSobeDTO.from(tipSobe);
    }

    @Transactional
    public void obrisi(Long id) {
        tipSobeRepository.delete(getTipSobe(id));
        tipSobeRepository.flush();
    }

    private void popuni(TipSobe tipSobe, String tip, TipSobeDTO dto) {
        tipSobe.setTip(tip);
        tipSobe.setMaxGostiju(dto.getMaxGostiju());
        tipSobe.setOpis(dto.getOpis());
    }
}
