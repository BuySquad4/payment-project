package com.bootcamp.paymentproject.user.controller;

import com.bootcamp.paymentproject.common.dto.CustomUserDetails;
import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.user.dto.request.SignUpRequest;
import com.bootcamp.paymentproject.user.dto.response.GetCurrentUserResponse;
import com.bootcamp.paymentproject.user.dto.response.SignUpResponse;
import com.bootcamp.paymentproject.common.security.JwtTokenProvider;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<SuccessResponse<GetCurrentUserResponse>> getCurrentUser(Authentication auth) {

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        // TODO: 구현
        // 데이터베이스에서 사용자 정보 조회
        // customerUid 생성은 조회 한 사용자 정보로 조합하여 생성, 추천 조합 : CUST_{userId}_{rand6:난수}
        // 임시 구현
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("email", email);
//        response.put("customerUid", "CUST_" + Math.abs(email.hashCode()));  // PortOne 고객 UID
//        response.put("name", email.split("@")[0]);  // 이메일에서 이름 추출
//        response.put("phone", "010-0000-0000");  // Kg 이니시스 전화번호 필수
//        response.put("pointBalance", 1000L);  // 포인트 잔액

        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(authService.getCurrentUser(principal.getEmail()), "사용자 정보 조회에 성공하였습니다."));
    }
}
