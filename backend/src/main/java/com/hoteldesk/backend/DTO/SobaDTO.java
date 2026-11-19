package com.hoteldesk.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoteldesk.backend.model.Soba;
import com.hoteldesk.backend.model.StatusSobe;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SobaDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;

    @NotNull
    private Integer brojSobe;

    private Integer sprat;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal cenaPoNoci;

    @JsonProperty(access = READ_ONLY)
    private StatusSobe status;

    @NotNull
    private Long tipSobeId;

    @JsonProperty(access = READ_ONLY)
    private String tipSobe;

    @JsonProperty(access = READ_ONLY)
    private Integer maxGostiju;

    @NotNull
    private Long hotelId;

    public static SobaDTO from(Soba s) {
        return new SobaDTO(s.getId(), s.getBrojSobe(), s.getSprat(), s.getCenaPoNoci(), s.getStatus(),
                s.getTipSobe().getId(), s.getTipSobe().getTip(), s.getTipSobe().getMaxGostiju(),
                s.getHotel().getId());
    }
}
