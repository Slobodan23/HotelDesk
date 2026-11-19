package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Vlasnik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VlasnikRepository extends JpaRepository<Vlasnik, Long> {

    Optional<Vlasnik> findByEmail(String email);
}
