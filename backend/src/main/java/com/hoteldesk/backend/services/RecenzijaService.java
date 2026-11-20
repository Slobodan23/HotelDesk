package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.RecenzijaDTO;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.ForbiddenException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.Recenzija;
import com.hoteldesk.backend.model.Rezervacija;
import com.hoteldesk.backend.model.StatusRezervacije;
import com.hoteldesk.backend.repository.RecenzijaRepository;
import com.hoteldesk.backend.repository.RezervacijaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecenzijaService {

    private final RecenzijaRepository recenzijaRepository;
    private final RezervacijaRepository rezervacijaRepository;
    private final KorisnikService korisnikService;

    public List<RecenzijaDTO> findByHotel(Long hotelId) {
        return recenzijaRepository.findByHotelIdOrderByDatumDesc(hotelId).stream().map(RecenzijaDTO::from).toList();
    }

    public List<RecenzijaDTO> mojeRecenzije(Long gostId) {
        return recenzijaRepository.findByGostIdOrderByDatumDesc(gostId).stream().map(RecenzijaDTO::from).toList();
    }

    @Transactional
    public RecenzijaDTO kreiraj(RecenzijaDTO dto, Long gostId) {
        Rezervacija rezervacija = rezervacijaRepository.findByIdAndGostId(dto.getRezervacijaId(), gostId)
                .orElseThrow(() -> new NotFoundException("Rezervacija nije pronadjena"));
        if (rezervacija.getStatus() != StatusRezervacije.ODJAVLJEN) {
            throw new BadRequestException("Recenziju je moguce ostaviti tek nakon odjave iz hotela");
        }
        if (recenzijaRepository.existsByRezervacijaId(rezervacija.getId())) {
            throw new ConflictException("Za ovu rezervaciju je vec ostavljena recenzija");
        }
        Recenzija recenzija = new Recenzija();
        recenzija.setOcena(dto.getOcena());
        recenzija.setTekstRecenzije(dto.getTekstRecenzije());
        recenzija.setGost(rezervacija.getGost());
        recenzija.setHotel(rezervacija.getSoba().getHotel());
        recenzija.setRezervacija(rezervacija);
        return RecenzijaDTO.from(recenzijaRepository.save(recenzija));
    }

    @Transactional
    public void obrisi(Long id, Long korisnikId) {
        Recenzija recenzija = recenzijaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recenzija nije pronadjena"));
        if (!recenzija.getGost().getId().equals(korisnikId) && !korisnikService.isAdmin(korisnikId)) {
            throw new ForbiddenException("Nemate pravo da obrisete ovu recenziju");
        }
        recenzijaRepository.delete(recenzija);
    }
}
