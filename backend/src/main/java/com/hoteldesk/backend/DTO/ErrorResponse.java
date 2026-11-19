package com.hoteldesk.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;

    private int status;

    private String message;

    private Map<String, String> errors;

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, message, Map.of());
    }

    public static ErrorResponse of(int status, String message, Map<String, String> errors) {
        return new ErrorResponse(LocalDateTime.now(), status, message, errors);
    }
}
