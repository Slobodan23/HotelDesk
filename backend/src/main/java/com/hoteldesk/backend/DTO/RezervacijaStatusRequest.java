package com.hoteldesk.backend.DTO;

import com.hoteldesk.backend.model.StatusRezervacije;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RezervacijaStatusRequest {

    @NotNull
    private StatusRezervacije status;
}
