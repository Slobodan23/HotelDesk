package com.hoteldesk.backend.DTO;

import com.hoteldesk.backend.model.StatusTiketa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TiketUpdateRequest {

    private StatusTiketa status;

    private Long dodeljenId;
}
