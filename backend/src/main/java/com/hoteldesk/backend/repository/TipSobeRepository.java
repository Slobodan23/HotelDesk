package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.TipSobe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipSobeRepository extends JpaRepository<TipSobe, Long> {

    boolean existsByTipIgnoreCase(String tip);
}
