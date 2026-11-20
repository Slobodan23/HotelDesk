package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Soba;
import com.hoteldesk.backend.model.StatusSobe;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SobaRepository extends JpaRepository<Soba, Long> {

    List<Soba> findByHotelIdOrderByBrojSobeAsc(Long hotelId);

    List<Soba> findByHotelIdAndStatus(Long hotelId, StatusSobe status);

    boolean existsByHotelIdAndBrojSobe(Long hotelId, Integer brojSobe);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Soba s where s.id = :id")
    Optional<Soba> zakljucaj(@Param("id") Long id);

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
