package com.bootcamp.paymentproject.user.service;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.membership.entity.Membership;
import com.bootcamp.paymentproject.membership.entity.UserMembership;
import com.bootcamp.paymentproject.membership.enums.MembershipGrade;
import com.bootcamp.paymentproject.membership.exception.MembershipException;
import com.bootcamp.paymentproject.membership.repository.MembershipRepository;
import com.bootcamp.paymentproject.membership.repository.UserMembershipRepository;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.user.dto.request.SignUpRequest;
import com.bootcamp.paymentproject.user.dto.response.GetCurrentUserResponse;
import com.bootcamp.paymentproject.user.dto.response.SignUpResponse;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.exception.UserException;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final UserMembershipRepository userMembershipRepository;

    private final MembershipRepository membershipRepository;

    private final PointTransactionRepository pointTransactionRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .username(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        userRepository.saveAndFlush(user);

        Membership membership = membershipRepository.findByGradeName(MembershipGrade.NORMAL)
                .orElseThrow(
                        () -> new MembershipException(ErrorCode.NOT_FOUND_GRADE)
                );

        UserMembership userMembership = UserMembership.builder()
                .totalAmount(new BigDecimal("0"))
                .user(user)
                .membership(membership)
                .build();

        userMembershipRepository.save(userMembership);

        return SignUpResponse.fromEntity(user);
    }

    @Transactional
    public GetCurrentUserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException(ErrorCode.NOT_FOUND_USER)
        );

        user.setPointBalance(pointTransactionRepository.getPointSumByUserId(user.getId(), PointType.EARN));

        return GetCurrentUserResponse.fromEntity(user);
    }
}
