package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Recenzija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecenzijaRepository extends JpaRepository<Recenzija, Long> {

    List<Recenzija> findByHotelIdOrderByDatumDesc(Long hotelId);

    List<Recenzija> findByGostIdOrderByDatumDesc(Long gostId);

    boolean existsByRezervacijaId(Long rezervacijaId);

    @Query("select avg(r.ocena) from Recenzija r where r.hotel.id = :hotelId")
    Double prosecnaOcena(@Param("hotelId") Long hotelId);
}
