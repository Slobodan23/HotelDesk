package com.hoteldesk.backend.DTO;

import com.hoteldesk.backend.model.StatusSobe;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SobaStatusRequest {

    @NotNull
    private StatusSobe status;
}
