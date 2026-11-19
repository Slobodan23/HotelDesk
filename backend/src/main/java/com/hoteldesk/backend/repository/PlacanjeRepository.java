package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Placanje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlacanjeRepository extends JpaRepository<Placanje, Long> {

    Optional<Placanje> findByRezervacijaId(Long rezervacijaId);
}
