package org.beaconfire.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.beaconfire.authentication.dto.request.TokenGenerationRequest;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.service.RegistrationTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationTokenController.class)
public class RegistrationTokenControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationTokenService registrationTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "HR")
    void testGenerationToken_Success() throws Exception {
        // Given
        TokenGenerationRequest request = new TokenGenerationRequest("test@company");
        User hrUser = User.builder().id(1).username("hr_user").build();

        RegistrationToken registrationToken = RegistrationToken.builder()
                .token("abc123")
                .email("test@company")
                .expirationDate(LocalDateTime.now().plusHours(3))
                .createdBy(hrUser)
                .build();

        when(registrationTokenService.generateToken(request.getEmail(), hrUser.getUsername())).thenReturn(registrationToken);

        // When & Then
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(registrationToken.getToken()))
                .andExpect(jsonPath("$.expiration").value(registrationToken.getExpirationDate().toString()))
                .andExpect(jsonPath("$.message").value("Registration token generated and sent via email."));
    }
}
