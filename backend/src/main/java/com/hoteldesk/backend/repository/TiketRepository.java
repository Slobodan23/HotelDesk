package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.StatusTiketa;
import com.hoteldesk.backend.model.Tiket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TiketRepository extends JpaRepository<Tiket, Long> {

    List<Tiket> findByHotelIdOrderByDatumKreiranjaDesc(Long hotelId);

    List<Tiket> findByHotelIdAndStatus(Long hotelId, StatusTiketa status);

    List<Tiket> findByDodeljenIdOrderByDatumKreiranjaDesc(Long zaposleniId);

    List<Tiket> findByKreiraoIdOrderByDatumKreiranjaDesc(Long korisnikId);

    List<Tiket> findByDodeljenId(Long zaposleniId);
}
