package com.jromax.bcisecurityuserservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
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
    
    @Email(message = "Email format is invalid")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email format is invalid")
    private String email;
    
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9].*[0-9])[a-z0-9A-Z]{8,12}$", 
             message = "Password must have exactly one uppercase letter, exactly two digits, lowercase letters, and be 8-12 characters long")
    private String password;
    
    @Valid
    private List<PhoneUserDTO> phones;
    
    private LocalDateTime created;
    
    private LocalDateTime lastLogin;
    
    private String token;
    
    private Boolean isActive;
}