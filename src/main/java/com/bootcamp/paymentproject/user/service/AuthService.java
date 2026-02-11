package com.bootcamp.paymentproject.user.service;

import com.bootcamp.paymentproject.user.dto.request.SignUpRequest;
import com.bootcamp.paymentproject.user.dto.response.GetCurrentUserResponse;
import com.bootcamp.paymentproject.user.dto.response.SignUpResponse;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        userRepository.save(user);

        return SignUpResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public GetCurrentUserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("일치하는 유저가 없습니다.")
        );

        return GetCurrentUserResponse.fromEntity(user);
    }
}
