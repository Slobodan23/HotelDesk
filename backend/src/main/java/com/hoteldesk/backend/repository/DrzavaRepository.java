package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Drzava;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrzavaRepository extends JpaRepository<Drzava, Long> {

    List<Drzava> findAllByOrderByNazivAsc();

    boolean existsByNazivIgnoreCase(String naziv);
}
