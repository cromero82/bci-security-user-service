package com.jromax.bcisecurityuserservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf(expression = "#{environment['app.env.enable-integration-test'] == 'true'}", loadContext = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Sign Up - Invalid Email should return 400 with validation error structure")
    void signUp_invalidEmail_returns400() throws Exception {
        System.out.println("[DEBUG_LOG] START: signUp_invalidEmail_returns400");
        String body = "{\n" +
                "  \"name\": \"Test User\",\n" +
                "  \"email\": \"invalid-email\",\n" +
                "  \"password\": \"Test1234\",\n" +
                "  \"phones\": [ { \"number\": 123456789, \"citycode\": 1, \"contrycode\": \"+1\" } ]\n" +
                "}";

        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error", isA(java.util.List.class)))
                .andExpect(jsonPath("$.error[0].timestamp", notNullValue()))
                .andExpect(jsonPath("$.error[0].codigo", is(400)))
                .andExpect(jsonPath("$.error[*].detail", hasItem(containsString("email"))));
        System.out.println("[DEBUG_LOG] END: signUp_invalidEmail_returns400");
    }

    @Test
    @DisplayName("Sign Up - Invalid Password should return 400 with validation error structure")
    void signUp_invalidPassword_returns400() throws Exception {
        System.out.println("[DEBUG_LOG] START: signUp_invalidPassword_returns400");
        String body = "{\n" +
                "  \"name\": \"Test User\",\n" +
                "  \"email\": \"test.user@example.com\",\n" +
                "  \"password\": \"weakpassword\",\n" +
                "  \"phones\": [ { \"number\": 123456789, \"citycode\": 1, \"contrycode\": \"+1\" } ]\n" +
                "}";

        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error", isA(java.util.List.class)))
                .andExpect(jsonPath("$.error[0].timestamp", notNullValue()))
                .andExpect(jsonPath("$.error[0].codigo", is(400)))
                .andExpect(jsonPath("$.error[*].detail", hasItem(containsString("password"))));
        System.out.println("[DEBUG_LOG] END: signUp_invalidPassword_returns400");
    }

    @Test
    @DisplayName("Sign Up - Password with multiple uppercase and >2 digits should return 400")
    void signUp_passwordWithMultipleUppercaseAndMoreDigits_returns400() throws Exception {
        System.out.println("[DEBUG_LOG] START: signUp_passwordWithMultipleUppercaseAndMoreDigits_returns400");
        // TEst1234 has two uppercase letters (T,E) and four digits (1,2,3,4) - should be invalid
        String body = "{\n" +
                "  \"name\": \"Test User\",\n" +
                "  \"email\": \"test.user.2@example.com\",\n" +
                "  \"password\": \"TEst1234\",\n" +
                "  \"phones\": [ { \"number\": 123456789, \"citycode\": 1, \"contrycode\": \"+1\" } ]\n" +
                "}";

        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error[0].codigo", is(400)))
                .andExpect(jsonPath("$.error[*].detail", hasItem(containsString("password"))));
        System.out.println("[DEBUG_LOG] END: signUp_passwordWithMultipleUppercaseAndMoreDigits_returns400");
    }

    @Test
    @DisplayName("Sign Up - Duplicate Email (seeded john.doe@example.com) should return 409 with conflict error")
    void signUp_duplicateEmail_returns409() throws Exception {
        System.out.println("[DEBUG_LOG] START: signUp_duplicateEmail_returns409");
        String body = "{\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"email\": \"john.doe@example.com\",\n" +
                "  \"password\": \"a2asfGfdfdf4\",\n" +
                "  \"phones\": [ { \"number\": 123456789, \"citycode\": 1, \"contrycode\": \"+1\" } ]\n" +
                "}";

        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error[0].codigo", is(409)))
                .andExpect(jsonPath("$.error[0].detail", anyOf(containsString("exists"), containsString("registered"), containsString("Email"))));
        System.out.println("[DEBUG_LOG] END: signUp_duplicateEmail_returns409");
    }

    @Test
    @DisplayName("Login - Invalid Token should return 400 with error structure")
    void login_invalidToken_returns400() throws Exception {
        System.out.println("[DEBUG_LOG] START: login_invalidToken_returns400");
        mockMvc.perform(post("/api/login")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error[0].codigo", is(400)))
                .andExpect(jsonPath("$.error[0].detail", containsString("Invalid token")));
        System.out.println("[DEBUG_LOG] END: login_invalidToken_returns400");
    }
}
