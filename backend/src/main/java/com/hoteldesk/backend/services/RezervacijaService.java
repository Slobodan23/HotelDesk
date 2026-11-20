package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.RezervacijaDTO;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.ForbiddenException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.*;
import com.hoteldesk.backend.repository.GostRepository;
import com.hoteldesk.backend.repository.RezervacijaRepository;
import com.hoteldesk.backend.repository.SobaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hoteldesk.backend.model.StatusRezervacije.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RezervacijaService {

    private static final Map<StatusRezervacije, Set<StatusRezervacije>> DOZVOLJENI_PRELAZI = Map.of(
            KREIRANA, Set.of(POTVRDJENA, OTKAZANA),
            POTVRDJENA, Set.of(PRIJAVLJEN, OTKAZANA),
            PRIJAVLJEN, Set.of(ODJAVLJEN),
            ODJAVLJEN, Set.of(),
            OTKAZANA, Set.of()
    );

    private final RezervacijaRepository rezervacijaRepository;
    private final SobaRepository sobaRepository;
    private final GostRepository gostRepository;
    private final KarticaService karticaService;
    private final HotelService hotelService;

    public Rezervacija getRezervacija(Long id) {
        return rezervacijaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rezervacija nije pronadjena"));
    }

    public RezervacijaDTO getById(Long id, Long korisnikId) {
        Rezervacija rezervacija = getRezervacija(id);
        if (!rezervacija.getGost().getId().equals(korisnikId)) {
            hotelService.proveriZaposlenje(rezervacija.getSoba().getHotel(), korisnikId);
        }
        return RezervacijaDTO.from(rezervacija);
    }

    public List<RezervacijaDTO> mojeRezervacije(Long gostId) {
        return rezervacijaRepository.findByGostIdOrderByDatumDolaskaDesc(gostId).stream()
                .map(RezervacijaDTO::from).toList();
    }

    public List<RezervacijaDTO> rezervacijeHotela(Long hotelId, StatusRezervacije status, Long korisnikId) {
        hotelService.proveriZaposlenje(hotelService.getHotel(hotelId), korisnikId);
        List<Rezervacija> rezervacije = status == null
                ? rezervacijaRepository.findBySobaHotelIdOrderByDatumDolaskaDesc(hotelId)
                : rezervacijaRepository.findBySobaHotelIdAndStatus(hotelId, status);
        return rezervacije.stream().map(RezervacijaDTO::from).toList();
    }

    public List<RezervacijaDTO> dolasci(Long hotelId, LocalDate datum, Long korisnikId) {
        hotelService.proveriZaposlenje(hotelService.getHotel(hotelId), korisnikId);
        return rezervacijaRepository.findBySobaHotelIdAndDatumDolaska(hotelId, datum).stream()
                .filter(r -> r.getStatus() != OTKAZANA)
                .map(RezervacijaDTO::from).toList();
    }

    public List<RezervacijaDTO> odlasci(Long hotelId, LocalDate datum, Long korisnikId) {
        hotelService.proveriZaposlenje(hotelService.getHotel(hotelId), korisnikId);
        return rezervacijaRepository.findBySobaHotelIdAndDatumOdlaska(hotelId, datum).stream()
                .filter(r -> r.getStatus() != OTKAZANA)
                .map(RezervacijaDTO::from).toList();
    }

    @Transactional
    public RezervacijaDTO kreiraj(RezervacijaDTO dto, Long gostId) {
        Gost gost = gostRepository.findById(gostId)
                .orElseThrow(() -> new ForbiddenException("Samo gost moze da napravi rezervaciju"));
        LocalDate dolazak = dto.getDatumDolaska();
        LocalDate odlazak = dto.getDatumOdlaska();
        if (dolazak.isBefore(LocalDate.now())) {
            throw new BadRequestException("Datum dolaska ne moze biti u proslosti");
        }
        if (!odlazak.isAfter(dolazak)) {
            throw new BadRequestException("Datum odlaska mora biti posle datuma dolaska");
        }

        Soba soba = sobaRepository.zakljucaj(dto.getSobaId())
                .orElseThrow(() -> new NotFoundException("Soba nije pronadjena"));
        if (dto.getBrojGostiju() > soba.getTipSobe().getMaxGostiju()) {
            throw new BadRequestException("Soba moze da primi najvise " + soba.getTipSobe().getMaxGostiju() + " gostiju");
        }
        if (rezervacijaRepository.postojiPreklapanje(soba.getId(), dolazak, odlazak)) {
            throw new ConflictException("Soba je zauzeta u izabranom periodu");
        }

        long brojNoci = ChronoUnit.DAYS.between(dolazak, odlazak);
        BigDecimal ukupnaCena = soba.getCenaPoNoci().multiply(BigDecimal.valueOf(brojNoci));

        Rezervacija rezervacija = new Rezervacija();
        rezervacija.setGost(gost);
        rezervacija.setSoba(soba);
        rezervacija.setDatumDolaska(dolazak);
        rezervacija.setDatumOdlaska(odlazak);
        rezervacija.setBrojGostiju(dto.getBrojGostiju());
        rezervacija.setUkupnaCena(ukupnaCena);

        Placanje placanje = new Placanje();
        placanje.setIznos(ukupnaCena);
        placanje.setNacinPlacanja(dto.getNacinPlacanja());
        placanje.setRezervacija(rezervacija);

        if (dto.getNacinPlacanja() == NacinPlacanja.KARTICA) {
            if (dto.getKarticaId() == null) {
                throw new BadRequestException("Za placanje karticom potrebno je izabrati karticu");
            }
            Kartica kartica = karticaService.getKarticaGosta(dto.getKarticaId(), gostId);
            if (karticaService.istekla(kartica)) {
                throw new BadRequestException("Kartica je istekla");
            }
            placanje.setKartica(kartica);
            placanje.setPlaceno(true);
            placanje.setDatumPlacanja(LocalDateTime.now());
            rezervacija.setStatus(POTVRDJENA);
        } else {
            rezervacija.setStatus(KREIRANA);
        }
        rezervacija.setPlacanje(placanje);

        return RezervacijaDTO.from(rezervacijaRepository.save(rezervacija));
    }

    @Transactional
    public RezervacijaDTO otkazi(Long id, Long gostId) {
        Rezervacija rezervacija = rezervacijaRepository.findByIdAndGostId(id, gostId)
                .orElseThrow(() -> new NotFoundException("Rezervacija nije pronadjena"));
        if (rezervacija.getStatus() != KREIRANA && rezervacija.getStatus() != POTVRDJENA) {
            throw new BadRequestException("Rezervacija se vise ne moze otkazati");
        }
        if (!rezervacija.getDatumDolaska().isAfter(LocalDate.now())) {
            throw new BadRequestException("Rezervacija se moze otkazati najkasnije dan pre dolaska");
        }
        rezervacija.setStatus(OTKAZANA);
        return RezervacijaDTO.from(rezervacija);
    }

    @Transactional
    public RezervacijaDTO promeniStatus(Long id, StatusRezervacije noviStatus, Long korisnikId) {
        Rezervacija rezervacija = getRezervacija(id);
        hotelService.proveriZaposlenje(rezervacija.getSoba().getHotel(), korisnikId);

        StatusRezervacije trenutni = rezervacija.getStatus();
        if (!DOZVOLJENI_PRELAZI.get(trenutni).contains(noviStatus)) {
            throw new BadRequestException("Nije moguce promeniti status iz " + trenutni + " u " + noviStatus);
        }
        if (noviStatus == PRIJAVLJEN && LocalDate.now().isBefore(rezervacija.getDatumDolaska())) {
            throw new BadRequestException("Prijava nije moguca pre datuma dolaska");
        }
        if (noviStatus == ODJAVLJEN) {
            Placanje placanje = rezervacija.getPlacanje();
            if (placanje != null && !placanje.isPlaceno()) {
                placanje.setPlaceno(true);
                placanje.setDatumPlacanja(LocalDateTime.now());
            }
            rezervacija.getSoba().setStatus(StatusSobe.PRLJAVA);
        }
        rezervacija.setStatus(noviStatus);
        return RezervacijaDTO.from(rezervacija);
    }
}
