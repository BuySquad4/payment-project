package com.bootcamp.paymentproject.user.service;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.membership.entity.Membership;
import com.bootcamp.paymentproject.membership.entity.UserMembership;
import com.bootcamp.paymentproject.membership.enums.MembershipGrade;
import com.bootcamp.paymentproject.membership.exception.MembershipException;
import com.bootcamp.paymentproject.membership.repository.MembershipRepository;
import com.bootcamp.paymentproject.membership.repository.UserMembershipRepository;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.user.dto.request.SignUpRequest;
import com.bootcamp.paymentproject.user.dto.response.SignUpResponse;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.exception.UserException;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private AuthService authService;

    private SignUpRequest request;

    @BeforeEach
    void setUp() {
        request = SignUpRequest.builder()
                .name("아무개")
                .email("test@gmail.com")
                .password("12345678")
                .phone("010-1234-5678")
                .build();
    }

    @Test
    void 회원가입_성공(){
        // given
        Membership membership = Membership.builder()
                .gradeName(MembershipGrade.NORMAL)
                .build();

        given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.empty());

        given(bCryptPasswordEncoder.encode(request.getPassword()))
                .willReturn("encodedPw");

        given(membershipRepository.findByGradeName(MembershipGrade.NORMAL))
                .willReturn(Optional.of(membership));

        // when
        SignUpResponse response = authService.signup(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@gmail.com");
        assertThat(response.getName()).isEqualTo("아무개");

        verify(userMembershipRepository, times(1)).save(any(UserMembership.class));
    }

    @Test
    void 회원가입_실패_이메일중복(){
        // given
        given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.of(User.builder().build()));

        // when
//        assertThatThrownBy(() -> authService.signup(request))
//                .isInstanceOf(UserException.class)
//                .hasMessage("중복된 이메일입니다");

        UserException exception = assertThrows(UserException.class,
                () -> authService.signup(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void 회원가입_실패_멤버십없음(){
        // given
        given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.empty());

        given(bCryptPasswordEncoder.encode(request.getPassword()))
                .willReturn("encodedPw");

        given(membershipRepository.findByGradeName(MembershipGrade.NORMAL))
                .willReturn(Optional.empty());

        // when
        MembershipException exception = assertThrows(MembershipException.class,
                () -> authService.signup(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_GRADE);
        
//        assertThatThrownBy(() -> authService.signup(request))
//                .isInstanceOf(MembershipException.class)
//                .hasMessage("존재하지 않는 등급입니다");
    }
}