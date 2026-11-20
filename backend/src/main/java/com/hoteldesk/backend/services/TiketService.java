package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.TiketDTO;
import com.hoteldesk.backend.DTO.TiketUpdateRequest;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ForbiddenException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.*;
import com.hoteldesk.backend.repository.RezervacijaRepository;
import com.hoteldesk.backend.repository.TiketRepository;
import com.hoteldesk.backend.repository.ZaposleniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TiketService {

    private final TiketRepository tiketRepository;
    private final ZaposleniRepository zaposleniRepository;
    private final RezervacijaRepository rezervacijaRepository;
    private final KorisnikService korisnikService;
    private final HotelService hotelService;
    private final SobaService sobaService;

    public Tiket getTiket(Long id) {
        return tiketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tiket nije pronadjen"));
    }

    public TiketDTO getById(Long id, Long korisnikId) {
        Tiket tiket = getTiket(id);
        if (tiket.getKreirao() == null || !tiket.getKreirao().getId().equals(korisnikId)) {
            hotelService.proveriZaposlenje(tiket.getHotel(), korisnikId);
        }
        return TiketDTO.from(tiket);
    }

    public List<TiketDTO> findByHotel(Long hotelId, StatusTiketa status, Long korisnikId) {
        hotelService.proveriZaposlenje(hotelService.getHotel(hotelId), korisnikId);
        List<Tiket> tiketi = status == null
                ? tiketRepository.findByHotelIdOrderByDatumKreiranjaDesc(hotelId)
                : tiketRepository.findByHotelIdAndStatus(hotelId, status);
        return tiketi.stream().map(TiketDTO::from).toList();
    }

    public List<TiketDTO> mojiTiketi(Long korisnikId) {
        return tiketRepository.findByKreiraoIdOrderByDatumKreiranjaDesc(korisnikId).stream()
                .map(TiketDTO::from).toList();
    }

    public List<TiketDTO> dodeljeniTiketi(Long zaposleniId) {
        return tiketRepository.findByDodeljenIdOrderByDatumKreiranjaDesc(zaposleniId).stream()
                .map(TiketDTO::from).toList();
    }

    @Transactional
    public TiketDTO kreiraj(TiketDTO dto, Long korisnikId) {
        Korisnik korisnik = korisnikService.getKorisnik(korisnikId);
        Hotel hotel = hotelService.getHotel(dto.getHotelId());
        if (korisnik instanceof Gost) {
            boolean smesten = rezervacijaRepository.existsByGostIdAndSobaHotelIdAndStatus(
                    korisnikId, hotel.getId(), StatusRezervacije.PRIJAVLJEN);
            if (!smesten) {
                throw new ForbiddenException("Tiket mozete prijaviti samo za hotel u kome ste trenutno smesteni");
            }
        } else {
            hotelService.proveriZaposlenje(hotel, korisnikId);
        }

        Tiket tiket = new Tiket();
        tiket.setHotel(hotel);
        tiket.setTipTiketa(dto.getTipTiketa());
        tiket.setSadrzaj(dto.getSadrzaj().trim());
        tiket.setKreirao(korisnik);
        if (dto.getSobaId() != null) {
            Soba soba = sobaService.getSoba(dto.getSobaId());
            if (!soba.getHotel().getId().equals(hotel.getId())) {
                throw new BadRequestException("Soba ne pripada izabranom hotelu");
            }
            tiket.setSoba(soba);
        }
        return TiketDTO.from(tiketRepository.save(tiket));
    }

    @Transactional
    public TiketDTO azuriraj(Long id, TiketUpdateRequest request, Long korisnikId) {
        Tiket tiket = getTiket(id);
        hotelService.proveriZaposlenje(tiket.getHotel(), korisnikId);

        if (request.getDodeljenId() != null) {
            Zaposleni zaposleni = zaposleniRepository.findById(request.getDodeljenId())
                    .orElseThrow(() -> new NotFoundException("Zaposleni nije pronadjen"));
            if (!zaposleni.getHotel().getId().equals(tiket.getHotel().getId())) {
                throw new BadRequestException("Zaposleni ne radi u ovom hotelu");
            }
            tiket.setDodeljen(zaposleni);
        }

        if (request.getStatus() != null && request.getStatus() != tiket.getStatus()) {
            tiket.setStatus(request.getStatus());
            if (request.getStatus() == StatusTiketa.ZATVOREN) {
                tiket.setDatumZatvaranja(LocalDateTime.now());
                if (tiket.getTipTiketa() == TipTiketa.ZAHTEV_ZA_CISCENJE && tiket.getSoba() != null) {
                    tiket.getSoba().setStatus(StatusSobe.CISTA);
                }
            } else {
                tiket.setDatumZatvaranja(null);
            }
        }
        return TiketDTO.from(tiket);
    }
}
