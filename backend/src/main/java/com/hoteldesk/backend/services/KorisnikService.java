package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.KorisnikDTO;
import com.hoteldesk.backend.DTO.PromenaLozinkeRequest;
import com.hoteldesk.backend.DTO.RegistracijaRequest;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.Admin;
import com.hoteldesk.backend.model.Gost;
import com.hoteldesk.backend.model.Korisnik;
import com.hoteldesk.backend.model.Vlasnik;
import com.hoteldesk.backend.repository.KorisnikRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KorisnikService {

    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;

    public Korisnik getKorisnik(Long id) {
        return korisnikRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Korisnik nije pronadjen"));
    }

    public KorisnikDTO getById(Long id) {
        return KorisnikDTO.from(getKorisnik(id));
    }

    public KorisnikDTO getByEmail(String email) {
        return korisnikRepository.findByEmail(normalizujEmail(email))
                .map(KorisnikDTO::from)
                .orElseThrow(() -> new NotFoundException("Korisnik nije pronadjen"));
    }

    public List<KorisnikDTO> findAll() {
        return korisnikRepository.findByObrisanFalse().stream().map(KorisnikDTO::from).toList();
    }

    public boolean isAdmin(Long korisnikId) {
        return getKorisnik(korisnikId) instanceof Admin;
    }

    @Transactional
    public KorisnikDTO registrujGosta(RegistracijaRequest request) {
        return KorisnikDTO.from(registruj(new Gost(), request));
    }

    @Transactional
    public KorisnikDTO registrujVlasnika(RegistracijaRequest request) {
        return KorisnikDTO.from(registruj(new Vlasnik(), request));
    }

    @Transactional
    public KorisnikDTO izmeniProfil(Long id, KorisnikDTO dto) {
        Korisnik korisnik = getKorisnik(id);
        korisnik.setIme(dto.getIme().trim());
        korisnik.setPrezime(dto.getPrezime().trim());
        korisnik.setBrojTelefona(dto.getBrojTelefona());
        korisnik.setPol(dto.getPol());
        return KorisnikDTO.from(korisnik);
    }

    @Transactional
    public void promeniLozinku(Long id, PromenaLozinkeRequest request) {
        Korisnik korisnik = getKorisnik(id);
        if (!passwordEncoder.matches(request.getStaraLozinka(), korisnik.getPassword())) {
            throw new BadRequestException("Stara lozinka nije ispravna");
        }
        korisnik.setPassword(passwordEncoder.encode(request.getNovaLozinka()));
    }

    public void proveriSlobodanEmail(String email) {
        if (korisnikRepository.existsByEmail(normalizujEmail(email))) {
            throw new ConflictException("Korisnik sa ovim email-om vec postoji");
        }
    }

    public String normalizujEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public String kodirajLozinku(String lozinka) {
        return passwordEncoder.encode(lozinka);
    }

    private <T extends Korisnik> T registruj(T korisnik, RegistracijaRequest request) {
        proveriSlobodanEmail(request.getEmail());
        korisnik.setIme(request.getIme().trim());
        korisnik.setPrezime(request.getPrezime().trim());
        korisnik.setEmail(normalizujEmail(request.getEmail()));
        korisnik.setPassword(passwordEncoder.encode(request.getPassword()));
        korisnik.setBrojTelefona(request.getBrojTelefona());
        korisnik.setPol(request.getPol());
        return korisnikRepository.save(korisnik);
    }
}
