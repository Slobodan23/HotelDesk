package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.AdresaDTO;
import com.hoteldesk.backend.DTO.HotelDTO;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ForbiddenException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.*;
import com.hoteldesk.backend.repository.HotelRepository;
import com.hoteldesk.backend.repository.RecenzijaRepository;
import com.hoteldesk.backend.repository.VlasnikRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;
    private final VlasnikRepository vlasnikRepository;
    private final RecenzijaRepository recenzijaRepository;
    private final KorisnikService korisnikService;
    private final GradService gradService;

    public Hotel getHotel(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel nije pronadjen"));
    }

    public List<HotelDTO> pretraga(Long gradId, String naziv, Integer minZvezdica) {
        String filter = naziv == null || naziv.isBlank() ? null : naziv.trim();
        return hotelRepository.pretraga(gradId, filter, minZvezdica).stream().map(this::toDto).toList();
    }

    public List<HotelDTO> findByVlasnik(Long vlasnikId) {
        return hotelRepository.findByVlasnikId(vlasnikId).stream().map(this::toDto).toList();
    }

    public HotelDTO getById(Long id) {
        return toDto(getHotel(id));
    }

    @Transactional
    public HotelDTO kreiraj(HotelDTO dto, Long korisnikId) {
        Korisnik korisnik = korisnikService.getKorisnik(korisnikId);
        Vlasnik vlasnik;
        if (korisnik instanceof Vlasnik v) {
            vlasnik = v;
        } else if (korisnik instanceof Admin) {
            if (dto.getVlasnikId() == null) {
                throw new BadRequestException("Potrebno je izabrati vlasnika hotela");
            }
            vlasnik = vlasnikRepository.findById(dto.getVlasnikId())
                    .orElseThrow(() -> new NotFoundException("Vlasnik nije pronadjen"));
        } else {
            throw new ForbiddenException("Samo vlasnik ili admin mogu da kreiraju hotel");
        }
        Hotel hotel = new Hotel();
        hotel.setVlasnik(vlasnik);
        hotel.setAdresa(new Adresa());
        popuni(hotel, dto);
        return toDto(hotelRepository.save(hotel));
    }

    @Transactional
    public HotelDTO izmeni(Long id, HotelDTO dto, Long korisnikId) {
        Hotel hotel = getHotel(id);
        proveriUpravljanje(hotel, korisnikId);
        popuni(hotel, dto);
        return toDto(hotel);
    }

    @Transactional
    public void obrisi(Long id, Long korisnikId) {
        Hotel hotel = getHotel(id);
        proveriVlasnistvo(hotel, korisnikId);
        hotelRepository.delete(hotel);
        hotelRepository.flush();
    }

    public void proveriVlasnistvo(Hotel hotel, Long korisnikId) {
        Korisnik korisnik = korisnikService.getKorisnik(korisnikId);
        if (korisnik instanceof Admin || jeVlasnik(hotel, korisnik)) {
            return;
        }
        throw new ForbiddenException("Nemate pravo pristupa ovom hotelu");
    }

    public void proveriUpravljanje(Hotel hotel, Long korisnikId) {
        Korisnik korisnik = korisnikService.getKorisnik(korisnikId);
        if (korisnik instanceof Admin || jeVlasnik(hotel, korisnik)) {
            return;
        }
        if (korisnik instanceof Zaposleni z && radiU(z, hotel) && z.getTipZaposlenog() == TipZaposlenog.MENADZER) {
            return;
        }
        throw new ForbiddenException("Nemate pravo upravljanja ovim hotelom");
    }

    public void proveriZaposlenje(Hotel hotel, Long korisnikId) {
        Korisnik korisnik = korisnikService.getKorisnik(korisnikId);
        if (korisnik instanceof Admin || jeVlasnik(hotel, korisnik)) {
            return;
        }
        if (korisnik instanceof Zaposleni z && radiU(z, hotel)) {
            return;
        }
        throw new ForbiddenException("Nemate pristup podacima ovog hotela");
    }

    private boolean jeVlasnik(Hotel hotel, Korisnik korisnik) {
        return korisnik instanceof Vlasnik && hotel.getVlasnik().getId().equals(korisnik.getId());
    }

    private boolean radiU(Zaposleni zaposleni, Hotel hotel) {
        return zaposleni.getHotel().getId().equals(hotel.getId());
    }

    private void popuni(Hotel hotel, HotelDTO dto) {
        hotel.setNaziv(dto.getNaziv().trim());
        hotel.setOpis(dto.getOpis());
        hotel.setBrojZvezdica(dto.getBrojZvezdica());
        hotel.setEmail(dto.getEmail());
        hotel.setBrojTelefona(dto.getBrojTelefona());
        AdresaDTO adresaDto = dto.getAdresa();
        Adresa adresa = hotel.getAdresa();
        adresa.setUlica(adresaDto.getUlica().trim());
        adresa.setBroj(adresaDto.getBroj().trim());
        adresa.setGrad(gradService.getGrad(adresaDto.getGradId()));
    }

    private HotelDTO toDto(Hotel hotel) {
        return HotelDTO.from(hotel, recenzijaRepository.prosecnaOcena(hotel.getId()));
    }
}
