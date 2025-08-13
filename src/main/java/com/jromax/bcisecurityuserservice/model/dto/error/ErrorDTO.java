package com.jromax.bcisecurityuserservice.model.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    
    private LocalDateTime timestamp;
    private Integer codigo;
    private String detail;
}