package com.jromax.bcisecurityuserservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneUserDTO {
    
    private UUID id;
    
    @NotNull(message = "Number is required")
    private Long number;
    
    @NotNull(message = "City code is required")
    private Integer citycode;
    
    @NotNull(message = "Country code is required")
    private String countrycode;
}