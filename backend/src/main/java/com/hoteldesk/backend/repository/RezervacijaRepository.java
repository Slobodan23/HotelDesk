package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Rezervacija;
import com.hoteldesk.backend.model.StatusRezervacije;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RezervacijaRepository extends JpaRepository<Rezervacija, Long> {

    List<Rezervacija> findByGostIdOrderByDatumDolaskaDesc(Long gostId);

    List<Rezervacija> findBySobaHotelIdOrderByDatumDolaskaDesc(Long hotelId);

    List<Rezervacija> findBySobaHotelIdAndStatus(Long hotelId, StatusRezervacije status);

    List<Rezervacija> findBySobaHotelIdAndDatumDolaska(Long hotelId, LocalDate datumDolaska);

    List<Rezervacija> findBySobaHotelIdAndDatumOdlaska(Long hotelId, LocalDate datumOdlaska);

    Optional<Rezervacija> findByIdAndGostId(Long id, Long gostId);

    boolean existsByGostIdAndSobaHotelIdAndStatus(Long gostId, Long hotelId, StatusRezervacije status);

    @Query("""
            select count(r) > 0 from Rezervacija r
            where r.soba.id = :sobaId
              and r.status <> com.hoteldesk.backend.model.StatusRezervacije.OTKAZANA
              and r.datumDolaska < :datumOdlaska
              and r.datumOdlaska > :datumDolaska
            """)
    boolean postojiPreklapanje(@Param("sobaId") Long sobaId,
                               @Param("datumDolaska") LocalDate datumDolaska,
                               @Param("datumOdlaska") LocalDate datumOdlaska);
}
