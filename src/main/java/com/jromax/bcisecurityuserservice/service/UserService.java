package com.jromax.bcisecurityuserservice.service;

import com.jromax.bcisecurityuserservice.model.dto.UserDTO;
import java.util.List;

public interface UserService {
    
    /**
     * Register a new user
     * 
     * @param userDTO the user data
     * @return the registered user with token
     */
    UserDTO registerUser(UserDTO userDTO);
    
    /**
     * Login a user using token
     * 
     * @param token the JWT token
     * @return the user with refreshed token
     */
    UserDTO loginUser(String token);
    
    /**
     * Get all users including password values
     * 
     * @return list of all users with their passwords
     */
    List<UserDTO> getAllUsersWithPasswords();
}