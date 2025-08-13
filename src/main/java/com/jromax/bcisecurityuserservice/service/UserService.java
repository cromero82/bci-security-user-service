package com.jromax.bcisecurityuserservice.service;

import com.jromax.bcisecurityuserservice.model.dto.UserDTO;

public interface UserService {
    
    /**
     * Register a new user
     * 
     * @param userDTO the user data
     * @return the registered user with token
     */
    UserDTO registerUser(UserDTO userDTO);
}