package com.bootcamp.paymentproject.common.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰 인증 필터
 * 모든 요청에서 JWT 토큰을 검증하고 SecurityContext에 인증 정보 설정
 *
 * TODO: 개선 사항
 * - 역할(Role) 정보를 토큰에서 추출
 * - 예외 처리 개선
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = getJwtFromRequest(request);

            // 2-1. 토큰 유효성 검증
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2-2. 토큰 만료 검증
            if (jwtTokenProvider.isExpired(token)){
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 토큰에서 사용자 정보 추출
            String email = jwtTokenProvider.getEmail(token);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // 4. 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 5. SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException e) {
            sendError(response, 401, "토큰이 만료되었거나 존재하지 않습니다.");
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write("""
        {
          "status": %d,
          "message": "%s"
        }
        """.formatted(status, message));
    }

    /**
     * Request Header에서 JWT 토큰 추출
     * Authorization: Bearer {token}
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
