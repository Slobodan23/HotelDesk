package com.hoteldesk.backend.repository;

import com.hoteldesk.backend.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByVlasnikId(Long vlasnikId);

    List<Hotel> findByAdresaGradId(Long gradId);

    boolean existsByIdAndVlasnikId(Long id, Long vlasnikId);

    @Query("""
            select h from Hotel h
            where (:gradId is null or h.adresa.grad.id = :gradId)
              and (:naziv is null or lower(h.naziv) like lower(concat('%', cast(:naziv as string), '%')))
              and (:minZvezdica is null or h.brojZvezdica >= :minZvezdica)
            order by h.naziv
            """)
    List<Hotel> pretraga(@Param("gradId") Long gradId,
                         @Param("naziv") String naziv,
                         @Param("minZvezdica") Integer minZvezdica);
}
