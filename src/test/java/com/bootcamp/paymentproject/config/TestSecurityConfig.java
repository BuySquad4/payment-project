package com.bootcamp.paymentproject.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 요청 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 1) 정적 리소스
                        .requestMatchers(toStaticResources().atCommonLocations()).permitAll()

                        // 2) 템플릿 페이지 렌더링
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pages/**").permitAll()

                        // 3) 공개 API
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/points/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        // 4) 인증 API
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/signup").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()

                        // PortOne Webhook (외부에서 들어오는 요청이라 인증 없이 허용)
                        .requestMatchers(HttpMethod.POST, "/portone-webhook").permitAll()

                        // 5) 그 외 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 6) 나머지 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .build();
    }
}