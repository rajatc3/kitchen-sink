package org.johndoe.kitchensink.controllers;

import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.dtos.requests.AuthRequest;
import org.johndoe.kitchensink.dtos.requests.AuthResponse;
import org.johndoe.kitchensink.dtos.requests.RefreshTokenRequest;
import org.johndoe.kitchensink.dtos.requests.RefreshTokenResponse;
import org.johndoe.kitchensink.services.KeycloakAuthService;
import org.johndoe.kitchensink.services.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Autowired  // ✅ Let Spring inject MockMvc
    private MockMvc mockMvc;

    @MockBean
    private KeycloakAuthService authService;

    @MockBean
    private MemberService memberService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testLogin() throws Exception {
        AuthResponse authResponse = new AuthResponse(1L, "access-token", "refresh-token", "admin");

        when(authService.login(any(AuthRequest.class))).thenReturn(Mono.just(authResponse));

        mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"admin\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.accessToken").value("access-token"));

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testRefreshToken() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh-token");
        RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse("new-access-token", "new-refresh-token", "3600", "Bearer");

        when(authService.refreshToken(anyString())).thenReturn(Mono.just(refreshTokenResponse));

        mockMvc.perform(post("/api/auth/refresh-token")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value("3600"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService, times(1)).refreshToken(anyString());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testRegister() throws Exception {
        MemberDto member = new MemberDto(1L, "jane.doe", "Jane", "Doe", "jane.doe@email.com", "9876543211", "user");

        when(authService.register(any(MemberDto.class))).thenReturn(Mono.empty());

        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"jane.doe\", \"firstName\": \"Jane\", \"lastName\": \"Doe\", \"email\": \"jane.doe@email.com\", \"phoneNumber\": \"9876543211\", \"userRole\": \"user\", \"password\": \"Password@123\", \"repeatPassword\": \"Password@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(authService).register(any(MemberDto.class)); // No explicit times() constraint
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCheckUsername() throws Exception {
        when(memberService.checkUsernameAvailability("jane.doe")).thenReturn(Mono.just(true));

        mockMvc.perform(get("/api/auth/check-username")
                        .param("username", "jane.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(memberService, times(1)).checkUsernameAvailability("jane.doe");
    }

}
