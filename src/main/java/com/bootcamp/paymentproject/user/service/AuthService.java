package com.bootcamp.paymentproject.user.service;

import com.bootcamp.paymentproject.common.dto.SignUpDto;
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
    public SignUpDto.Response signup(SignUpDto.Request request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhoneNumber())
                .email(request.getEmail())
                .build();

        userRepository.save(user);

        return SignUpDto.Response.fromEntity(user);
    }
}
