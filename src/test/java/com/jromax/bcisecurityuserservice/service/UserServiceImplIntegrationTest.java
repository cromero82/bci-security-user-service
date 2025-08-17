package com.jromax.bcisecurityuserservice.service;

import com.jromax.bcisecurityuserservice.model.dto.PhoneUserDTO;
import com.jromax.bcisecurityuserservice.model.dto.UserDTO;
import com.jromax.bcisecurityuserservice.model.entity.User;
import com.jromax.bcisecurityuserservice.repository.UserRepository;
import com.jromax.bcisecurityuserservice.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIf(expression = "#{environment['app.env.enable-integration-test'] == 'true'}", loadContext = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Environment environment;


    @Test
    @DisplayName("Should have seeded users from import.sql present")
    void seededUsersArePresent() {
        System.out.println("[DEBUG_LOG] START: Should have seeded users from import.sql present");
        Optional<User> john = userRepository.findByEmail("john.doe@example.com");
        Optional<User> jane = userRepository.findByEmail("jane.smith@example.com");
        System.out.println("[DEBUG_LOG] Fetched users -> johnPresent=" + john.isPresent() + ", janePresent=" + jane.isPresent());
        assertTrue(john.isPresent(), "Expected john.doe@example.com to be present from import.sql");
        assertTrue(jane.isPresent(), "Expected jane.smith@example.com to be present from import.sql");
        System.out.println("[DEBUG_LOG] END: Should have seeded users from import.sql present");
    }

    @Test
    @DisplayName("registerUser should persist, encode password and generate token")
    void registerUser_persistsAndGeneratesToken() {
        System.out.println("[DEBUG_LOG] START: registerUser should persist, encode password and generate token");
        // Given
        UserDTO request = UserDTO.builder()
                .name("Test User")
                .email("integration.user@example.com")
                .password("Test1234")
                .phones(Arrays.asList(
                        PhoneUserDTO.builder()
                                .number(123456789L)
                                .citycode(1)
                                .contrycode("+1")
                                .build()
                ))
                .build();

        System.out.println("[DEBUG_LOG] Request prepared: " + request);
        // When
        UserDTO created = userService.registerUser(request);
        System.out.println("[DEBUG_LOG] User created with ID=" + created.getId() + ", token length=" + (created.getToken() == null ? 0 : created.getToken().length()));

        // Then
        assertNotNull(created.getId(), "User ID should be generated");
        assertNotNull(created.getToken(), "Token should be generated and saved");
        assertEquals(request.getEmail(), created.getEmail());

        User stored = userRepository.findByEmail(request.getEmail()).orElseThrow();
        System.out.println("[DEBUG_LOG] Stored user found: id=" + stored.getId());
        assertNotNull(stored.getPassword(), "Stored password should not be null");
        assertNotEquals("Test1234", stored.getPassword(), "Password should be encoded and not equal to raw");
        assertEquals(created.getToken(), stored.getToken(), "Token on DTO should match persisted token");
        System.out.println("[DEBUG_LOG] END: registerUser should persist, encode password and generate token");
    }

    @Test
    @DisplayName("loginUser should refresh token and update lastLogin using existing token")
    void loginUser_refreshesTokenAndUpdatesLastLogin() {
        System.out.println("[DEBUG_LOG] START: loginUser should refresh token and update lastLogin using existing token");
        // Arrange: create a user first to get a valid token
        UserDTO request = UserDTO.builder()
                .name("Login Test")
                .email("login.user@example.com")
                .password("Test1234")
                .build();
        UserDTO created = userService.registerUser(request);
        System.out.println("[DEBUG_LOG] Created user for login test with id=" + created.getId());

        User before = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String previousToken = before.getToken();
        LocalDateTime lastLoginBefore = before.getLastLogin();
        System.out.println("[DEBUG_LOG] Previous token length=" + (previousToken == null ? 0 : previousToken.length()) + ", lastLoginBefore=" + lastLoginBefore);

        // Act
        try {
            Thread.sleep(10); // ensure new token has a different issuedAt timestamp
        } catch (InterruptedException ignored) {}

        UserDTO afterLogin = userService.loginUser(previousToken);
        System.out.println("[DEBUG_LOG] After login: new token length=" + (afterLogin.getToken() == null ? 0 : afterLogin.getToken().length()) + ", lastLogin=" + afterLogin.getLastLogin());

        // Assert
        assertNotNull(afterLogin.getToken());
        assertFalse(afterLogin.getToken().isEmpty(), "Token must not be empty after login");
        assertNotNull(afterLogin.getLastLogin());

        User persistedAfter = userRepository.findByEmail(request.getEmail()).orElseThrow();
        assertEquals(afterLogin.getToken(), persistedAfter.getToken(), "Persisted token must match response");
        assertTrue(persistedAfter.getLastLogin().isAfter(lastLoginBefore) || persistedAfter.getLastLogin().isEqual(lastLoginBefore),
                "lastLogin should be updated to now or later than previous value");

        // Also ensure password is present in response (as per current requirements)
        assertNotNull(afterLogin.getPassword(), "Password must be present in response as per requirement");
        System.out.println("[DEBUG_LOG] END: loginUser should refresh token and update lastLogin using existing token");
    }

    @Test
    @DisplayName("getAllUsersWithPasswords should return users including seeded ones with passwords")
    void getAllUsersWithPasswords_returnsSeededUsers() {
        System.out.println("[DEBUG_LOG] START: getAllUsersWithPasswords should return users including seeded ones with passwords");
        // When
        List<UserDTO> users = userService.getAllUsersWithPasswords();
        System.out.println("[DEBUG_LOG] Retrieved users count=" + (users == null ? 0 : users.size()));

        // Then
        assertNotNull(users);
        assertTrue(users.size() >= 2, "Should have at least the two seeded users");
        assertTrue(users.stream().allMatch(u -> u.getPassword() != null), "All returned users should include password");
        System.out.println("[DEBUG_LOG] END: getAllUsersWithPasswords should return users including seeded ones with passwords");
    }
}
