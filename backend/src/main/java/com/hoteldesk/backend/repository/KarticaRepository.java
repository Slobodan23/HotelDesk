package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Kartica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KarticaRepository extends JpaRepository<Kartica, Long> {

    List<Kartica> findByGostId(Long gostId);

    Optional<Kartica> findByIdAndGostId(Long id, Long gostId);
}
