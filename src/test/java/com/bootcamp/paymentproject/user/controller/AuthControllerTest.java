package com.bootcamp.paymentproject.user.controller;

import com.bootcamp.paymentproject.common.security.CustomUserDetails;
import com.bootcamp.paymentproject.common.security.CustomUserDetailsService;
import com.bootcamp.paymentproject.common.security.JwtTokenProvider;
import com.bootcamp.paymentproject.config.TestSecurityConfig;
import com.bootcamp.paymentproject.user.dto.request.SignUpRequest;
import com.bootcamp.paymentproject.user.dto.response.GetCurrentUserResponse;
import com.bootcamp.paymentproject.user.dto.response.SignUpResponse;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
//@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider; // 🔥 추가

    @MockitoBean
    CustomUserDetailsService customUserDetailsService; // 🔥 추가

    @Test
    void 회원가입_성공시_201_반환() throws Exception {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .name("아무개")
                .password("12345678")
                .phone("010-1234-5678")
                .email("test@gmil.com")
                .build();

        SignUpResponse response = SignUpResponse.builder()
                .name("아무개")
                .phoneNumber("010-1234-5678")
                .email("test@gmil.com")
                .createdAt(LocalDateTime.now())
                .build();

        given(authService.signup(request)).willReturn(response);

        // when, then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void 회원정보_조회() throws Exception {
        // given
        User user = User.builder()
                .username("아무개")
                .password("12345678")
                .phone("010-1234-56789")
                .email("test@gmail.com")
                .pointBalance(BigDecimal.ZERO)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                );

        GetCurrentUserResponse userResponse = GetCurrentUserResponse.fromEntity(user);

        given(authService.getCurrentUser("test@gmail.com")).willReturn(userResponse);

        // when, then
        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}