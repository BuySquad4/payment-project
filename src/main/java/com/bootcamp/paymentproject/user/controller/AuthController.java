package com.bootcamp.paymentproject.user.controller;

import com.bootcamp.paymentproject.common.security.CustomUserDetails;
import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.user.dto.request.SignUpRequest;
import com.bootcamp.paymentproject.user.dto.response.GetCurrentUserResponse;
import com.bootcamp.paymentproject.user.dto.response.SignUpResponse;
import com.bootcamp.paymentproject.common.security.JwtTokenProvider;
import com.bootcamp.paymentproject.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 * 구현할 API 엔드포인트 템플릿
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signup(
            @Valid @RequestBody SignUpRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success(authService.signup(request), "회원가입에 성공했습니다."));
    }

    /**
     * 현재 로그인한 사용자 정보 조회 API
     * GET /api/auth/me
     *
     * 응답:
     * {
     *   "success": true,
     *   "email": "user@example.com",
     *   "customerUid": "CUST_xxxxx",
     *   "name": "홍길동"
     * }
     *
     * 중요: customerUid는 PortOne 빌링키 발급 시 활용!
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<GetCurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponse.success(
                        authService.getCurrentUser(principal.getEmail()),
                        "사용자 정보 조회에 성공하였습니다."
                ));
    }
}
