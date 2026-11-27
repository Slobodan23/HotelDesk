package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Korisnik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

    Optional<Korisnik> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Korisnik> findByObrisanFalse();
}
