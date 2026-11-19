package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.TipZaposlenog;
import com.hoteldesk.backend.model.Zaposleni;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZaposleniRepository extends JpaRepository<Zaposleni, Long> {

    Optional<Zaposleni> findByEmail(String email);

    List<Zaposleni> findByHotelId(Long hotelId);

    List<Zaposleni> findByHotelIdAndTipZaposlenog(Long hotelId, TipZaposlenog tipZaposlenog);
}
