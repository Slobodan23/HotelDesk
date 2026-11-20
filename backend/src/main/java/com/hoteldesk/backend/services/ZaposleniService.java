package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.ZaposleniDTO;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.Hotel;
import com.hoteldesk.backend.model.Zaposleni;
import com.hoteldesk.backend.repository.TiketRepository;
import com.hoteldesk.backend.repository.ZaposleniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZaposleniService {

    private final ZaposleniRepository zaposleniRepository;
    private final TiketRepository tiketRepository;
    private final HotelService hotelService;
    private final KorisnikService korisnikService;

    public Zaposleni getZaposleni(Long id) {
        return zaposleniRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zaposleni nije pronadjen"));
    }

    public List<ZaposleniDTO> findByHotel(Long hotelId, Long korisnikId) {
        hotelService.proveriUpravljanje(hotelService.getHotel(hotelId), korisnikId);
        return zaposleniRepository.findByHotelId(hotelId).stream().map(ZaposleniDTO::from).toList();
    }

    public ZaposleniDTO getById(Long id, Long korisnikId) {
        Zaposleni zaposleni = getZaposleni(id);
        if (!zaposleni.getId().equals(korisnikId)) {
            hotelService.proveriUpravljanje(zaposleni.getHotel(), korisnikId);
        }
        return ZaposleniDTO.from(zaposleni);
    }

    @Transactional
    public ZaposleniDTO kreiraj(ZaposleniDTO dto, Long korisnikId) {
        Hotel hotel = hotelService.getHotel(dto.getHotelId());
        hotelService.proveriUpravljanje(hotel, korisnikId);
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BadRequestException("Lozinka je obavezna");
        }
        korisnikService.proveriSlobodanEmail(dto.getEmail());
        Zaposleni zaposleni = new Zaposleni();
        zaposleni.setEmail(korisnikService.normalizujEmail(dto.getEmail()));
        zaposleni.setPassword(korisnikService.kodirajLozinku(dto.getPassword()));
        zaposleni.setHotel(hotel);
        popuni(zaposleni, dto);
        return ZaposleniDTO.from(zaposleniRepository.save(zaposleni));
    }

    @Transactional
    public ZaposleniDTO izmeni(Long id, ZaposleniDTO dto, Long korisnikId) {
        Zaposleni zaposleni = getZaposleni(id);
        hotelService.proveriUpravljanje(zaposleni.getHotel(), korisnikId);
        if (!zaposleni.getHotel().getId().equals(dto.getHotelId())) {
            Hotel noviHotel = hotelService.getHotel(dto.getHotelId());
            hotelService.proveriUpravljanje(noviHotel, korisnikId);
            zaposleni.setHotel(noviHotel);
        }
        String email = korisnikService.normalizujEmail(dto.getEmail());
        if (!zaposleni.getEmail().equals(email)) {
            korisnikService.proveriSlobodanEmail(email);
            zaposleni.setEmail(email);
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            zaposleni.setPassword(korisnikService.kodirajLozinku(dto.getPassword()));
        }
        popuni(zaposleni, dto);
        return ZaposleniDTO.from(zaposleni);
    }

    @Transactional
    public void obrisi(Long id, Long korisnikId) {
        Zaposleni zaposleni = getZaposleni(id);
        hotelService.proveriUpravljanje(zaposleni.getHotel(), korisnikId);
        tiketRepository.findByDodeljenId(id).forEach(t -> t.setDodeljen(null));
        zaposleniRepository.delete(zaposleni);
        zaposleniRepository.flush();
    }

    private void popuni(Zaposleni zaposleni, ZaposleniDTO dto) {
        zaposleni.setIme(dto.getIme().trim());
        zaposleni.setPrezime(dto.getPrezime().trim());
        zaposleni.setBrojTelefona(dto.getBrojTelefona());
        zaposleni.setPol(dto.getPol());
        zaposleni.setTipZaposlenog(dto.getTipZaposlenog());
    }
}
