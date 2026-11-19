package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Grad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradRepository extends JpaRepository<Grad, Long> {

    List<Grad> findByDrzavaIdOrderByNazivAsc(Long drzavaId);

    boolean existsByNazivIgnoreCaseAndDrzavaId(String naziv, Long drzavaId);
}
