package com.hoteldesk.backend.DTO;

import com.hoteldesk.backend.model.NacinPlacanja;
import com.hoteldesk.backend.model.Placanje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlacanjeDTO {

    private Long id;

    private BigDecimal iznos;

    private NacinPlacanja nacinPlacanja;

    private boolean placeno;

    private LocalDateTime datumPlacanja;

    private KarticaDTO kartica;

    public static PlacanjeDTO from(Placanje p) {
        if (p == null) {
            return null;
        }
        return new PlacanjeDTO(p.getId(), p.getIznos(), p.getNacinPlacanja(), p.isPlaceno(),
                p.getDatumPlacanja(), KarticaDTO.from(p.getKartica()));
    }
}
