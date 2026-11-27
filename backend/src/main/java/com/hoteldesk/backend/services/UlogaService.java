package com.hoteldesk.backend.services;

import com.hoteldesk.backend.DTO.KorisnikDTO;
import com.hoteldesk.backend.DTO.KreiranjeKorisnikaRequest;
import com.hoteldesk.backend.DTO.IzmenaKorisnikaRequest;
import com.hoteldesk.backend.exception.BadRequestException;
import com.hoteldesk.backend.exception.ConflictException;
import com.hoteldesk.backend.exception.NotFoundException;
import com.hoteldesk.backend.model.*;
import com.hoteldesk.backend.repository.HotelRepository;
import com.hoteldesk.backend.repository.KorisnikRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UlogaService {

    private static final Map<Uloga, String> TABELE = Map.of(
            Uloga.ADMIN, "admini",
            Uloga.VLASNIK, "vlasnici",
            Uloga.ZAPOSLENI, "zaposleni",
            Uloga.GOST, "gosti"
    );

    private final KorisnikService korisnikService;
    private final KorisnikRepository korisnikRepository;
    private final HotelRepository hotelRepository;
    private final EntityManager entityManager;

    @Transactional
    public KorisnikDTO kreiraj(KreiranjeKorisnikaRequest request) {
        korisnikService.proveriSlobodanEmail(request.getEmail());
        Korisnik korisnik = switch (request.getUloga()) {
            case ADMIN -> new Admin();
            case VLASNIK -> new Vlasnik();
            case GOST -> new Gost();
            case ZAPOSLENI -> {
                Zaposleni zaposleni = new Zaposleni();
                zaposleni.setTipZaposlenog(request.getTipZaposlenog());
                zaposleni.setHotel(getHotel(request.getHotelId()));
                yield zaposleni;
            }
        };
        korisnik.setIme(request.getIme().trim());
        korisnik.setPrezime(request.getPrezime().trim());
        korisnik.setEmail(korisnikService.normalizujEmail(request.getEmail()));
        korisnik.setPassword(korisnikService.kodirajLozinku(request.getPassword()));
        korisnik.setBrojTelefona(request.getBrojTelefona());
        korisnik.setPol(request.getPol());
        return KorisnikDTO.from(korisnikRepository.save(korisnik));
    }

    @Transactional
    public KorisnikDTO izmeni(Long id, IzmenaKorisnikaRequest request, Long adminId) {
        Korisnik korisnik = getAktivan(id);
        String email = korisnikService.normalizujEmail(request.getEmail());
        if (!email.equals(korisnik.getEmail())) {
            korisnikService.proveriSlobodanEmail(email);
        }
        korisnik.setIme(request.getIme().trim());
        korisnik.setPrezime(request.getPrezime().trim());
        korisnik.setEmail(email);
        korisnik.setBrojTelefona(request.getBrojTelefona());
        korisnik.setPol(request.getPol());
        if (request.getNovaLozinka() != null && !request.getNovaLozinka().isBlank()) {
            korisnik.setPassword(korisnikService.kodirajLozinku(request.getNovaLozinka()));
        }
        return promeniUlogu(korisnik, request, adminId);
    }

    @Transactional
    public void obrisi(Long id, Long adminId) {
        if (id.equals(adminId)) {
            throw new BadRequestException("Ne mozete obrisati svoj nalog");
        }
        Korisnik korisnik = getAktivan(id);
        Uloga uloga = KorisnikDTO.ulogaOf(korisnik);
        switch (uloga) {
            case VLASNIK -> {
                if (postoji("hoteli", "vlasnik_id", id)) {
                    throw new ConflictException("Vlasnik ima hotele, pa nalog ne moze da se obrise");
                }
            }
            case ADMIN -> proveriDaOstajeAdmin();
            case GOST -> {
                Number uHotelu = (Number) entityManager
                        .createNativeQuery("select count(*) from rezervacije where gost_id = :id and status = 'PRIJAVLJEN'")
                        .setParameter("id", id)
                        .getSingleResult();
                if (uHotelu.longValue() > 0) {
                    throw new ConflictException("Gost je trenutno prijavljen u hotelu, pa nalog ne moze da se obrise");
                }
                entityManager.createNativeQuery("update rezervacije set status = 'OTKAZANA' where gost_id = :id and status in ('KREIRANA', 'POTVRDJENA')")
                        .setParameter("id", id)
                        .executeUpdate();
            }
            case ZAPOSLENI -> oslobodiTikete(id);
        }
        korisnik.setObrisan(true);
        korisnik.setDatumBrisanja(LocalDateTime.now());
    }

    private KorisnikDTO promeniUlogu(Korisnik korisnik, IzmenaKorisnikaRequest request, Long adminId) {
        Long id = korisnik.getId();
        Uloga stara = KorisnikDTO.ulogaOf(korisnik);
        Uloga nova = request.getUloga();
        Zaposleni zaposleni = Hibernate.unproxy(korisnik) instanceof Zaposleni z ? z : null;

        boolean istaUloga = stara == nova && (nova != Uloga.ZAPOSLENI
                || (zaposleni.getTipZaposlenog() == request.getTipZaposlenog()
                && zaposleni.getHotel().getId().equals(request.getHotelId())));
        if (istaUloga) {
            return KorisnikDTO.from(korisnik);
        }
        if (id.equals(adminId)) {
            throw new BadRequestException("Ne mozete promeniti svoju ulogu");
        }

        if (zaposleni != null && (nova != Uloga.ZAPOSLENI || !zaposleni.getHotel().getId().equals(request.getHotelId()))) {
            oslobodiTikete(id);
        }

        if (stara == nova) {
            zaposleni.setTipZaposlenog(request.getTipZaposlenog());
            zaposleni.setHotel(getHotel(request.getHotelId()));
            return KorisnikDTO.from(zaposleni);
        }

        proveriDaMozeDaOde(id, stara);
        if (nova == Uloga.ZAPOSLENI) {
            getHotel(request.getHotelId());
        }

        entityManager.flush();
        entityManager.clear();

        if (stara == Uloga.GOST) {
            entityManager.createNativeQuery("delete from kartice where gost_id = :id").setParameter("id", id).executeUpdate();
        }
        entityManager.createNativeQuery("delete from " + TABELE.get(stara) + " where id = :id")
                .setParameter("id", id)
                .executeUpdate();
        if (nova == Uloga.ZAPOSLENI) {
            entityManager.createNativeQuery("insert into zaposleni (id, hotel_id, tip_zaposlenog) values (:id, :hotel, :tip)")
                    .setParameter("id", id)
                    .setParameter("hotel", request.getHotelId())
                    .setParameter("tip", request.getTipZaposlenog().name())
                    .executeUpdate();
        } else {
            entityManager.createNativeQuery("insert into " + TABELE.get(nova) + " (id) values (:id)")
                    .setParameter("id", id)
                    .executeUpdate();
        }
        entityManager.clear();
        return KorisnikDTO.from(korisnikService.getKorisnik(id));
    }

    private void oslobodiTikete(Long zaposleniId) {
        entityManager.createNativeQuery("update tiketi set dodeljen_id = null where dodeljen_id = :id and status <> 'ZATVOREN'")
                .setParameter("id", zaposleniId)
                .executeUpdate();
    }

    private void proveriDaOstajeAdmin() {
        Number admina = (Number) entityManager
                .createNativeQuery("select count(*) from admini a join korisnici k on k.id = a.id where k.obrisan = false")
                .getSingleResult();
        if (admina.longValue() <= 1) {
            throw new ConflictException("Sistem mora imati bar jednog admina");
        }
    }

    private Korisnik getAktivan(Long id) {
        Korisnik korisnik = korisnikService.getKorisnik(id);
        if (korisnik.isObrisan()) {
            throw new NotFoundException("Korisnik nije pronadjen");
        }
        return korisnik;
    }

    private void proveriDaMozeDaOde(Long id, Uloga stara) {
        if (stara == Uloga.GOST && (postoji("rezervacije", "gost_id", id) || postoji("recenzije", "gost_id", id))) {
            throw new ConflictException("Gost ima rezervacije ili recenzije, pa mu uloga ne moze da se promeni");
        }
        if (stara == Uloga.VLASNIK && postoji("hoteli", "vlasnik_id", id)) {
            throw new ConflictException("Vlasnik ima hotele, pa mu uloga ne moze da se promeni");
        }
        if (stara == Uloga.ADMIN) {
            proveriDaOstajeAdmin();
        }
    }

    private boolean postoji(String tabela, String kolona, Long id) {
        Number broj = (Number) entityManager
                .createNativeQuery("select count(*) from " + tabela + " where " + kolona + " = :id")
                .setParameter("id", id)
                .getSingleResult();
        return broj.longValue() > 0;
    }

    private Hotel getHotel(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel nije pronadjen"));
    }
}
