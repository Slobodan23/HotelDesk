package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.SobaDTO;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.Hotel;
import com.hoteldesk.backend.model.Soba;
import com.hoteldesk.backend.model.StatusSobe;
import com.hoteldesk.backend.repository.SobaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SobaService {

    private final SobaRepository sobaRepository;
    private final HotelService hotelService;
    private final TipSobeService tipSobeService;

    public Soba getSoba(Long id) {
        return sobaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Soba nije pronadjena"));
    }

    public SobaDTO getById(Long id) {
        return SobaDTO.from(getSoba(id));
    }

    public List<SobaDTO> findByHotel(Long hotelId) {
        return sobaRepository.findByHotelIdOrderByBrojSobeAsc(hotelId).stream().map(SobaDTO::from).toList();
    }

    public List<SobaDTO> findByHotelAndStatus(Long hotelId, StatusSobe status, Long korisnikId) {
        hotelService.proveriZaposlenje(hotelService.getHotel(hotelId), korisnikId);
        return sobaRepository.findByHotelIdAndStatus(hotelId, status).stream().map(SobaDTO::from).toList();
    }

    public List<SobaDTO> slobodneSobe(Long hotelId, LocalDate datumDolaska, LocalDate datumOdlaska, int brojGostiju) {
        if (!datumOdlaska.isAfter(datumDolaska)) {
            throw new BadRequestException("Datum odlaska mora biti posle datuma dolaska");
        }
        return sobaRepository.slobodneSobe(hotelId, datumDolaska, datumOdlaska, Math.max(brojGostiju, 1))
                .stream().map(SobaDTO::from).toList();
    }

    @Transactional
    public SobaDTO kreiraj(SobaDTO dto, Long korisnikId) {
        Hotel hotel = hotelService.getHotel(dto.getHotelId());
        hotelService.proveriUpravljanje(hotel, korisnikId);
        if (sobaRepository.existsByHotelIdAndBrojSobe(hotel.getId(), dto.getBrojSobe())) {
            throw new ConflictException("Soba sa ovim brojem vec postoji u hotelu");
        }
        Soba soba = new Soba();
        soba.setHotel(hotel);
        popuni(soba, dto);
        return SobaDTO.from(sobaRepository.save(soba));
    }

    @Transactional
    public SobaDTO izmeni(Long id, SobaDTO dto, Long korisnikId) {
        Soba soba = getSoba(id);
        hotelService.proveriUpravljanje(soba.getHotel(), korisnikId);
        if (!soba.getBrojSobe().equals(dto.getBrojSobe())
                && sobaRepository.existsByHotelIdAndBrojSobe(soba.getHotel().getId(), dto.getBrojSobe())) {
            throw new ConflictException("Soba sa ovim brojem vec postoji u hotelu");
        }
        popuni(soba, dto);
        return SobaDTO.from(soba);
    }

    @Transactional
    public SobaDTO promeniStatus(Long id, StatusSobe status, Long korisnikId) {
        Soba soba = getSoba(id);
        hotelService.proveriZaposlenje(soba.getHotel(), korisnikId);
        soba.setStatus(status);
        return SobaDTO.from(soba);
    }

    @Transactional
    public void obrisi(Long id, Long korisnikId) {
        Soba soba = getSoba(id);
        hotelService.proveriUpravljanje(soba.getHotel(), korisnikId);
        sobaRepository.delete(soba);
        sobaRepository.flush();
    }

    private void popuni(Soba soba, SobaDTO dto) {
        soba.setBrojSobe(dto.getBrojSobe());
        soba.setSprat(dto.getSprat());
        soba.setCenaPoNoci(dto.getCenaPoNoci());
        soba.setTipSobe(tipSobeService.getTipSobe(dto.getTipSobeId()));
    }
}
