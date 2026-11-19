package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Soba;
import com.hoteldesk.backend.model.StatusSobe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SobaRepository extends JpaRepository<Soba, Long> {

    List<Soba> findByHotelIdOrderByBrojSobeAsc(Long hotelId);

    List<Soba> findByHotelIdAndStatus(Long hotelId, StatusSobe status);

    boolean existsByHotelIdAndBrojSobe(Long hotelId, Integer brojSobe);

    @Query("""
            select s from Soba s
            where s.hotel.id = :hotelId
              and s.tipSobe.maxGostiju >= :brojGostiju
              and not exists (
                  select r from Rezervacija r
                  where r.soba = s
                    and r.status <> com.hoteldesk.backend.model.StatusRezervacije.OTKAZANA
                    and r.datumDolaska < :datumOdlaska
                    and r.datumOdlaska > :datumDolaska
              )
            order by s.cenaPoNoci, s.brojSobe
            """)
    List<Soba> slobodneSobe(@Param("hotelId") Long hotelId,
                            @Param("datumDolaska") LocalDate datumDolaska,
                            @Param("datumOdlaska") LocalDate datumOdlaska,
                            @Param("brojGostiju") int brojGostiju);
}
