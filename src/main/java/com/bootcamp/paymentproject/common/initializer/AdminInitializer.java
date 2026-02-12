package com.bootcamp.paymentproject.common.initializer;

import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        User admin = User.builder()
                .username("testname")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin"))
                .phone("010-1234-5678")
                .build();

        userRepository.save(admin);
    }
}