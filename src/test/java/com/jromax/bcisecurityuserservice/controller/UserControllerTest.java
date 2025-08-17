package com.jromax.bcisecurityuserservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jromax.bcisecurityuserservice.model.dto.PhoneUserDTO;
import com.jromax.bcisecurityuserservice.model.dto.UserDTO;
import com.jromax.bcisecurityuserservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/sign-up — returns 201 with created user (happy path)")
    void signUp_returns201WithUserDTO() throws Exception {
        System.out.println("[DEBUG_LOG] START: signUp_returns201WithUserDTO");
        // Arrange request and mocked response (based on import.sql-like data)
        UserDTO request = UserDTO.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("Test1234")
                .phones(Arrays.asList(
                        PhoneUserDTO.builder().number(123456789L).citycode(1).contrycode("+1").build()
                ))
                .build();

        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserDTO response = UserDTO.builder()
                .id(id)
                .name("John Doe")
                .email("john.doe@example.com")
                .created(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .token("mock-jwt-token")
                .isActive(true)
                .phones(Arrays.asList(
                        PhoneUserDTO.builder().number(123456789L).citycode(1).contrycode("+1").build()
                ))
                .build();

        given(userService.registerUser(any(UserDTO.class))).willReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.token", is("mock-jwt-token")))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.phones", hasSize(1)))
                .andExpect(jsonPath("$.phones[0].number", is(123456789)));

        System.out.println("[DEBUG_LOG] END: signUp_returns201WithUserDTO");
    }

    @Test
    @DisplayName("POST /api/login — returns 200 and refreshes user (happy path)")
    void login_returns200AndCallsServiceWithBearerTokenStripped() throws Exception {
        System.out.println("[DEBUG_LOG] START: login_returns200AndCallsServiceWithBearerTokenStripped");
        // Arrange
        String originalToken = "eyJhbGciOiJIUzI1NiJ9.mocktoken";
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UserDTO logged = UserDTO.builder()
                .id(id)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .lastLogin(LocalDateTime.now())
                .token("refreshed-token")
                .isActive(true)
                .phones(List.of(
                        PhoneUserDTO.builder().number(555123456L).citycode(3).contrycode("+44").build()
                ))
                .password("$2a$10$dummypasshash") // as per service behavior returning password on login
                .build();

        given(userService.loginUser(eq(originalToken))).willReturn(logged);

        // Act + Assert
        mockMvc.perform(post("/api/login")
                        .header("Authorization", "Bearer " + originalToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.email", is("jane.smith@example.com")))
                .andExpect(jsonPath("$.token", is("refreshed-token")))
                .andExpect(jsonPath("$.phones", hasSize(1)))
                .andExpect(jsonPath("$.password", not(emptyString())));

        // Verify the controller stripped the Bearer prefix and passed only the token
        verify(userService).loginUser(eq(originalToken));
        System.out.println("[DEBUG_LOG] END: login_returns200AndCallsServiceWithBearerTokenStripped");
    }

    @Test
    @DisplayName("GET /api/users/all-with-passwords — returns 200 with users list (happy path)")
    void getAllUsersWithPasswords_returns200WithList() throws Exception {
        System.out.println("[DEBUG_LOG] START: getAllUsersWithPasswords_returns200WithList");
        // Arrange list similar to import.sql seed
        UserDTO user1 = UserDTO.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("John Doe")
                .email("john.doe@example.com")
                .password("$2a$10$seedhash1")
                .phones(Arrays.asList(
                        PhoneUserDTO.builder().number(123456789L).citycode(1).contrycode("+1").build(),
                        PhoneUserDTO.builder().number(987654321L).citycode(2).contrycode("+1").build()
                ))
                .isActive(true)
                .build();

        UserDTO user2 = UserDTO.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .password("$2a$10$seedhash2")
                .phones(List.of(
                        PhoneUserDTO.builder().number(555123456L).citycode(3).contrycode("+44").build()
                ))
                .isActive(true)
                .build();

        given(userService.getAllUsersWithPasswords()).willReturn(Arrays.asList(user1, user2));

        // Act + Assert
        mockMvc.perform(get("/api/users/all-with-passwords"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[0].phones", hasSize(2)))
                .andExpect(jsonPath("$[1].email", is("jane.smith@example.com")))
                .andExpect(jsonPath("$[1].phones", hasSize(1)));

        System.out.println("[DEBUG_LOG] END: getAllUsersWithPasswords_returns200WithList");
    }
}
