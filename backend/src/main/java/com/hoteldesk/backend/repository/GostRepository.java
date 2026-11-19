package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Gost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GostRepository extends JpaRepository<Gost, Long> {

    Optional<Gost> findByEmail(String email);
}
