package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TiketDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotNull
    private Long hotelId;

    private Long sobaId;

    @NotNull
    private TipTiketa tipTiketa;

    @NotBlank
    @Size(max = 2000)
    private String sadrzaj;

    @JsonProperty(access = READ_ONLY)
    private StatusTiketa status;

    @JsonProperty(access = READ_ONLY)
    private LocalDateTime datumKreiranja;

    @JsonProperty(access = READ_ONLY)
    private LocalDateTime datumZatvaranja;

    @JsonProperty(access = READ_ONLY)
    private Integer brojSobe;

    @JsonProperty(access = READ_ONLY)
    private Long kreiraoId;

    @JsonProperty(access = READ_ONLY)
    private String kreiraoImePrezime;

    @JsonProperty(access = READ_ONLY)
    private Long dodeljenId;

    @JsonProperty(access = READ_ONLY)
    private String dodeljenImePrezime;

    public static TiketDTO from(Tiket t) {
        Soba s = t.getSoba();
        Korisnik k = t.getKreirao();
        Zaposleni z = t.getDodeljen();
        return new TiketDTO(t.getId(), t.getHotel().getId(),
                s != null ? s.getId() : null,
                t.getTipTiketa(), t.getSadrzaj(), t.getStatus(),
                t.getDatumKreiranja(), t.getDatumZatvaranja(),
                s != null ? s.getBrojSobe() : null,
                k != null ? k.getId() : null,
                k != null ? k.getIme() + " " + k.getPrezime() : null,
                z != null ? z.getId() : null,
                z != null ? z.getIme() + " " + z.getPrezime() : null);
    }
}
