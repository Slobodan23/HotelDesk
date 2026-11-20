package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.KarticaDTO;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ForbiddenException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.Gost;
import com.hoteldesk.backend.model.Kartica;
import com.hoteldesk.backend.repository.GostRepository;
import com.hoteldesk.backend.repository.KarticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KarticaService {

    private final KarticaRepository karticaRepository;
    private final GostRepository gostRepository;

    public List<KarticaDTO> mojeKartice(Long gostId) {
        return karticaRepository.findByGostId(gostId).stream().map(KarticaDTO::from).toList();
    }

    public Kartica getKarticaGosta(Long id, Long gostId) {
        return karticaRepository.findByIdAndGostId(id, gostId)
                .orElseThrow(() -> new NotFoundException("Kartica nije pronadjena"));
    }

    public boolean istekla(Kartica kartica) {
        return YearMonth.of(kartica.getGodinaIsteka(), kartica.getMesecIsteka()).isBefore(YearMonth.now());
    }

    @Transactional
    public KarticaDTO dodaj(KarticaDTO dto, Long gostId) {
        Gost gost = gostRepository.findById(gostId)
                .orElseThrow(() -> new ForbiddenException("Samo gost moze da doda karticu"));
        Kartica kartica = new Kartica();
        kartica.setImeNosioca(dto.getImeNosioca().trim());
        kartica.setMesecIsteka(dto.getMesecIsteka());
        kartica.setGodinaIsteka(dto.getGodinaIsteka());
        if (istekla(kartica)) {
            throw new BadRequestException("Kartica je istekla");
        }
        String broj = dto.getBrojKartice();
        kartica.setPoslednjeCetiriCifre(broj.substring(broj.length() - 4));
        kartica.setToken(UUID.randomUUID().toString());
        kartica.setGost(gost);
        return KarticaDTO.from(karticaRepository.save(kartica));
    }

    @Transactional
    public void obrisi(Long id, Long gostId) {
        karticaRepository.delete(getKarticaGosta(id, gostId));
        karticaRepository.flush();
    }
}
