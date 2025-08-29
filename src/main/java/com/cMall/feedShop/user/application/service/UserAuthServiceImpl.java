package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.email.EmailService;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.exception.AccountNotVerifiedException;
import com.cMall.feedShop.user.domain.model.PasswordResetToken;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.PasswordResetTokenRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final JwtTokenProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${app.password-reset-url}")
    private String passwordResetBaseUrl;

    public UserAuthServiceImpl(UserRepository userRepository, UserProfileRepository userProfileRepository,
                               PasswordEncoder passwordEncoder,
                               PasswordResetTokenRepository passwordResetTokenRepository,
                               @Qualifier("emailServiceImpl") EmailService emailService, // 빈 이름 확인 필요
                               JwtTokenProvider jwtProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
    }

    /**
     * 사용자 로그인 처리 메서드.
     * 로그인 ID와 비밀번호를 받아 사용자 인증을 수행하고, 성공 시 JWT 토큰을 발급합니다.
     *
     * @param request 사용자 로그인 요청 (로그인 ID, 비밀번호 포함)
     * @return 로그인 응답 (JWT 토큰, 로그인 ID, 사용자 역할 포함)
     * @throws BusinessException 사용자가 존재하지 않거나 비밀번호가 일치하지 않을 경우 발생
     */

    public UserLoginResponse login(UserLoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        try {
            // 먼저 사용자 상태를 확인
            User user = userRepository.findByEmailWithProfile(request.getEmail())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 회원입니다."));
            
            // 사용자가 존재하고 DELETED 상태인 경우
            if (user != null && user.getStatus() == UserStatus.DELETED) {
                throw new BusinessException(ErrorCode.USER_ALREADY_DELETED, "탈퇴된 계정입니다. 새로운 계정으로 가입해주세요.");
            }

            // AuthenticationManager가 CustomUserDetailsService를 통해 사용자를 로드하고 비밀번호를 검증합니다.
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            if (user.getStatus() == UserStatus.PENDING) {
                throw new AccountNotVerifiedException("이메일 인증이 완료되지 않은 계정입니다.");
            }

            String nickname = null;
            String name = null;
            UserProfile userProfile = user.getUserProfile();
            if (userProfile != null) {
                nickname = userProfile.getNickname();
                name = userProfile.getName();
            }
            
            // UserProfile이 없으면 기본값 사용
            if (nickname == null || nickname.trim().isEmpty()) {
                nickname = user.getLoginId();
            }
            if (name == null || name.trim().isEmpty()) {
                name = user.getLoginId();
            }

            String token = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());

            return UserLoginResponse.builder()
                    .loginId(user.getLoginId())
                    .role(user.getRole())
                    .token(token)
                    .nickname(nickname)
                    .name(name)
                    .requiresMfa(false)
                    .tempToken(null)
                    .email(user.getEmail())
                    .build();
        } catch (UsernameNotFoundException e) {
            // 사용자를 찾을 수 없을 때 (CustomUserDetailsService에서 발생)
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 회원입니다.");
        } catch (org.springframework.security.core.AuthenticationException e) {
            // 비밀번호 불일치 등 인증 실패 (AuthenticationManager에서 발생)
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 활성 사용자만 비밀번호 재설정 가능 (선택 사항)
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_NOT_ACTIVE);
        }

        // 기존에 해당 유저의 유효한 토큰이 있다면 삭제 (새로운 토큰 발급을 위해)
        passwordResetTokenRepository.deleteByUser(user);

        // 새 토큰 생성 및 저장
        PasswordResetToken token = new PasswordResetToken(user);
        passwordResetTokenRepository.save(token);

        // 이메일 전송
        String resetLink = passwordResetBaseUrl + "?token="  + token.getToken(); // 프론트엔드 비밀번호 재설정 페이지 URL
        String emailSubject = "[cMall] 비밀번호 재설정 안내";
        String emailContent = "안녕하세요, " + user.getLoginId() + "님.<br><br>"
                + "비밀번호를 재설정하시려면 다음 링크를 클릭해주세요: <a href=\"" + resetLink + "\">비밀번호 재설정</a><br><br>"
                + "이 링크는 24시간 동안 유효합니다.<br>"
                + "만약 이 요청을 하지 않았다면, 이 이메일을 무시해주세요.";

        emailService.sendHtmlEmail(user.getEmail(), emailSubject, emailContent);
    }

    @Transactional(readOnly = true)
    public void validatePasswordResetToken(String tokenValue) {
        try {
            PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 비밀번호 재설정 토큰입니다."));

            if (token.isExpired()) {
                // 만료된 토큰은 여기서 삭제하지 않습니다.
                // 실제 비밀번호 재설정 (POST /reset-password) 시에 삭제하는 것이 좋습니다.
                // GET 요청에서는 단순히 만료되었다는 정보만 제공합니다.
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "비밀번호 재설정 토큰이 만료되었습니다. 새로운 재설정 링크를 요청해주세요.");
            }
            // 토큰이 유효하면 아무것도 반환하지 않고 메서드 종료 (컨트롤러로 제어권 반환)
        } catch (BusinessException e) {
            // BusinessException은 그대로 재抛出
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 로그를 남기고 일반적인 에러 메시지 제공
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비밀번호 재설정 토큰 검증 중 오류가 발생했습니다.");
        }
    }

    public void resetPassword(String tokenValue, String newPassword) {
        try {
            PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 비밀번호 재설정 토큰입니다."));

            if (token.isExpired()) {
                passwordResetTokenRepository.delete(token);
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "비밀번호 재설정 토큰이 만료되었습니다. 새로운 재설정 링크를 요청해주세요.");
            }

            User user = token.getUser();

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordChangedAt(LocalDateTime.now());
            userRepository.save(user);

            passwordResetTokenRepository.delete(token);
        } catch (BusinessException e) {
            // BusinessException은 그대로 재抛出
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 로그를 남기고 일반적인 에러 메시지 제공
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비밀번호 재설정 중 오류가 발생했습니다.");
        }
    }

    @Override
    public UserLoginResponse completeMfaLogin(String email) {
        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 계정 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_NOT_ACTIVE, "비활성화된 계정입니다.");
        }

        // MFA 인증이 완료된 상태에서 최종 로그인 토큰 발급
        String finalToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());

        // UserProfile 정보 명시적으로 조회
        String nickname = null;
        String name = null;
        var userProfileOpt = userProfileRepository.findByUser(user);
        if (userProfileOpt.isPresent()) {
            var userProfile = userProfileOpt.get();
            nickname = userProfile.getNickname();
            name = userProfile.getName();
        }
        
        // UserProfile이 없으면 기본값 사용
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = user.getLoginId();
        }
        if (name == null || name.trim().isEmpty()) {
            name = user.getLoginId();
        }

        return UserLoginResponse.builder()
                .loginId(user.getLoginId())
                .role(user.getRole())
                .token(finalToken) // 최종 로그인 토큰
                .nickname(nickname)
                .name(name)
                .requiresMfa(false) // MFA 인증 완료
                .tempToken(null) // 임시 토큰 제거
                .email(user.getEmail())
                .build();
    }


}
