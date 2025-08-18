package com.jromax.bcisecurityuserservice.service.impl;

import com.jromax.bcisecurityuserservice.model.dto.PhoneUserDTO;
import com.jromax.bcisecurityuserservice.model.dto.UserDTO;
import com.jromax.bcisecurityuserservice.model.entity.PhoneUser;
import com.jromax.bcisecurityuserservice.model.entity.User;
import com.jromax.bcisecurityuserservice.repository.UserRepository;
import com.jromax.bcisecurityuserservice.service.UserService;
import com.jromax.bcisecurityuserservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO registerUser(UserDTO userDTO) {
        log.info("Starting user registration process for email: {}", userDTO.getEmail());
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                log.warn("Registration failed: Email already registered: {}", userDTO.getEmail());
                throw new EntityExistsException("Email already registered");
            }

            log.debug("Creating user entity for email: {}", userDTO.getEmail());
            // Create user entity
            User user = User.builder()
                    .name(userDTO.getName())
                    .email(userDTO.getEmail())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .isActive(true)
                    .created(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .phones(new ArrayList<>())
                    .build();

            // Add phones if provided
            if (userDTO.getPhones() != null && !userDTO.getPhones().isEmpty()) {
                log.debug("Adding {} phone(s) to user", userDTO.getPhones().size());
                List<PhoneUser> phones = userDTO.getPhones().stream()
                        .map(this::mapToPhoneEntity)
                        .collect(Collectors.toList());
                
                phones.forEach(user::addPhone);
            }

            log.debug("Saving user to database");
            // Save user
            User savedUser = userRepository.save(user);

            log.debug("Generating JWT token for user");
            // Generate JWT token
            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId());
            savedUser.setToken(token);
            userRepository.save(savedUser);

            log.info("User registration completed successfully for ID: {}", savedUser.getId());
            // Map to DTO and return
            return mapToUserDTO(savedUser);
        } catch (EntityExistsException e) {
            // This is already logged above
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user registration: {}", e.getMessage());
            throw e;
        }
    }

    private PhoneUser mapToPhoneEntity(PhoneUserDTO phoneUserDTO) {
        return PhoneUser.builder()
                .number(phoneUserDTO.getNumber())
                .citycode(phoneUserDTO.getCitycode())
                .contrycode(phoneUserDTO.getContrycode())
                .build();
    }

    private UserDTO mapToUserDTO(User user) {
        List<PhoneUserDTO> phoneDTOs = user.getPhones().stream()
                .map(this::mapToPhoneDTO)
                .collect(Collectors.toList());

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .created(user.getCreated())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isActive(user.getIsActive())
                .phones(phoneDTOs)
                .build();
    }

    private PhoneUserDTO mapToPhoneDTO(PhoneUser phone) {
        return PhoneUserDTO.builder()
                .id(phone.getId())
                .number(phone.getNumber())
                .citycode(phone.getCitycode())
                .contrycode(phone.getContrycode())
                .build();
    }
    
    @Override
    @Transactional
    public UserDTO loginUser(String token) {
        log.info("Processing login request with token");
        try {
            // Extract user ID from token
            log.debug("Extracting user ID from token");
            UUID userId = jwtUtil.extractUserId(token);
            
            // Find user by ID
            log.debug("Finding user by ID: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found with ID: {}", userId);
                        return new EntityNotFoundException("User not found");
                    });
            
            // Validate token
            log.debug("Validating token for user: {}", user.getEmail());
            if (!jwtUtil.validateToken(token, user.getEmail())) {
                log.warn("Invalid or expired token for user: {}", user.getEmail());
                throw new IllegalArgumentException("Invalid or expired token");
            }
            
            // Update last login time
            log.debug("Updating last login time for user: {}", user.getEmail());
            user.setLastLogin(LocalDateTime.now());
            
            // Generate new token
            log.debug("Generating new token for user: {}", user.getEmail());
            String newToken = jwtUtil.generateToken(user.getEmail(), user.getId());
            user.setToken(newToken);
            
            // Save updated user
            log.debug("Saving updated user information");
            User updatedUser = userRepository.save(user);

            UserDTO userDTO = mapToUserDTO(updatedUser);

            // Include password in response as per requirements
            // Note: In a real-world scenario, we would not return the hashed password
            userDTO.setPassword(user.getPassword());
            
            log.info("Login successful for user: {}", user.getEmail());
            return userDTO;
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            log.error("Login failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersWithPasswords() {
        log.info("Retrieving all users with passwords");
        try {
            List<User> users = userRepository.findAll();
            log.debug("Found {} users in the database", users.size());
            
            List<UserDTO> userDTOs = users.stream()
                    .map(user -> {
                        UserDTO dto = mapToUserDTO(user);
                        // Include password in each user DTO
                        dto.setPassword(user.getPassword());
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            log.info("Successfully retrieved all users with passwords");
            return userDTOs;
        } catch (Exception e) {
            log.error("Error retrieving all users: {}", e.getMessage());
            throw e;
        }
    }
}