package com.jromax.bcisecurityuserservice.controller;

import com.jromax.bcisecurityuserservice.model.dto.UserDTO;
import com.jromax.bcisecurityuserservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO registeredUser = userService.registerUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@RequestHeader("Authorization") String authHeader) {
        // Extract token from Authorization header (Bearer token)
        String token = authHeader.startsWith("Bearer ") ? 
                authHeader.substring(7) : authHeader;
        
        UserDTO loggedInUser = userService.loginUser(token);
        return ResponseEntity.ok(loggedInUser);
    }
}