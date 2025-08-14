package com.jromax.bcisecurityuserservice.controller;

import com.jromax.bcisecurityuserservice.model.dto.UserDTO;
import com.jromax.bcisecurityuserservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("Received sign-up request for email: {}", userDTO.getEmail());
        try {
            UserDTO registeredUser = userService.registerUser(userDTO);
            log.info("User successfully registered with ID: {}", registeredUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (Exception e) {
            log.error("Error during user registration for email {}: {}", userDTO.getEmail(), e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@RequestHeader("Authorization") String authHeader) {
        log.info("Received login request");
        try {
            // Extract token from Authorization header (Bearer token)
            String token = authHeader.startsWith("Bearer ") ? 
                    authHeader.substring(7) : authHeader;
            
            log.debug("Processing login with token");
            UserDTO loggedInUser = userService.loginUser(token);
            log.info("User successfully logged in with ID: {}", loggedInUser.getId());
            return ResponseEntity.ok(loggedInUser);
        } catch (Exception e) {
            log.error("Error during user login: {}", e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/users/all-with-passwords")
    public ResponseEntity<List<UserDTO>> getAllUsersWithPasswords() {
        log.info("Received request to get all users with passwords");
        try {
            List<UserDTO> users = userService.getAllUsersWithPasswords();
            log.info("Successfully retrieved {} users with passwords", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving all users with passwords: {}", e.getMessage());
            throw e;
        }
    }
}