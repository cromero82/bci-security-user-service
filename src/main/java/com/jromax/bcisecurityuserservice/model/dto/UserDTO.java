package com.jromax.bcisecurityuserservice.model.dto;

import com.jromax.bcisecurityuserservice.validation.PropertyPattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private UUID id;
    
    private String name;
    
    @PropertyPattern(property = "app.validation.email-regex", message = "", allowEmpty = false)
    private String email;
    
    @PropertyPattern(
            property = "app.validation.password-regex",
            message = "",
            allowEmpty = false)
    private String password;
    
    @Valid
    private List<PhoneUserDTO> phones;
    
    private LocalDateTime created;
    
    private LocalDateTime lastLogin;
    
    private String token;
    
    private Boolean isActive;
}