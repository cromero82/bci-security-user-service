package com.jromax.bcisecurityuserservice.service.impl;

import com.jromax.bcisecurityuserservice.model.dto.PhoneUserDTO;
import com.jromax.bcisecurityuserservice.model.dto.UserDTO;
import com.jromax.bcisecurityuserservice.model.entity.PhoneUser;
import com.jromax.bcisecurityuserservice.model.entity.User;
import com.jromax.bcisecurityuserservice.repository.UserRepository;
import com.jromax.bcisecurityuserservice.service.UserService;
import com.jromax.bcisecurityuserservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO registerUser(UserDTO userDTO) {
        // Check if user already exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EntityExistsException("Email already registered");
        }

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
            List<PhoneUser> phones = userDTO.getPhones().stream()
                    .map(this::mapToPhoneEntity)
                    .collect(Collectors.toList());
            
            phones.forEach(user::addPhone);
        }

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId());
        savedUser.setToken(token);
        userRepository.save(savedUser);

        // Map to DTO and return
        return mapToUserDTO(savedUser);
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
        try {
            // Extract user ID from token
            UUID userId = jwtUtil.extractUserId(token);
            
            // Find user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            // Validate token
            if (!jwtUtil.validateToken(token, user.getEmail())) {
                throw new IllegalArgumentException("Invalid or expired token");
            }
            
            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            
            // Generate new token
            String newToken = jwtUtil.generateToken(user.getEmail(), user.getId());
            user.setToken(newToken);
            
            // Save updated user
            User updatedUser = userRepository.save(user);
            
            // Map to DTO and return
            UserDTO userDTO = mapToUserDTO(updatedUser);
            // Include password in response as per requirements
            // Note: In a real-world scenario, we would not return the hashed password
            // but the requirements specifically ask for it
            userDTO.setPassword(user.getPassword());
            
            return userDTO;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage());
        }
    }
}