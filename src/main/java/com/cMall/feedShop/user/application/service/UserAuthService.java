package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.service.EmailService;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.exception.AccountNotVerifiedException;
import com.cMall.feedShop.user.domain.model.PasswordResetToken;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.PasswordResetTokenRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final JwtTokenProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${app.password-reset-url}")
    private String passwordResetBaseUrl;

    /**
     * 사용자 로그인 처리 메서드.
     * 로그인 ID와 비밀번호를 받아 사용자 인증을 수행하고, 성공 시 JWT 토큰을 발급합니다.
     *
     * @param request 사용자 로그인 요청 (로그인 ID, 비밀번호 포함)
     * @return 로그인 응답 (JWT 토큰, 로그인 ID, 사용자 역할 포함)
     * @throws BusinessException 사용자가 존재하지 않거나 비밀번호가 일치하지 않을 경우 발생
     */

    public UserLoginResponse login(UserLoginRequest request) {
        // Spring Security의 AuthenticationManager를 사용하여 인증 시도
        // React에서 email을 보내고 있으므로, email을 사용자명으로 사용합니다.
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        try {
            // AuthenticationManager가 CustomUserDetailsService를 통해 사용자를 로드하고 비밀번호를 검증합니다.
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 인증 성공 후, 사용자 정보 로드 (CustomUserDetailsService에서 이미 이메일로 찾았음)
            // JWT 토큰 생성에 필요한 정보를 얻기 위해 User 객체를 다시 조회합니다.
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 회원입니다."));

            if (!user.isEnabled()) {
                throw new AccountNotVerifiedException("이메일 인증이 완료되지 않은 계정입니다.");
            }

            String nickname = null;
            if (user.getUserProfile() != null) {
                nickname = user.getUserProfile().getNickname();
            }

            String token = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());

            return new UserLoginResponse(user.getLoginId(), user.getRole(), token, nickname);
        } catch (UsernameNotFoundException e) {
            // 사용자를 찾을 수 없을 때 (CustomUserDetailsService에서 발생)
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 회원입니다.");
        } catch (org.springframework.security.core.AuthenticationException e) {
            // 비밀번호 불일치 등 인증 실패 (AuthenticationManager에서 발생)
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    // 1. 비밀번호 재설정 요청 (사용자 이메일 입력)
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

    // --- 여기에 validatePasswordResetToken 메서드를 추가해야 합니다. ---
    @Transactional(readOnly = true) // 이 메서드는 조회만 하므로 읽기 전용 트랜잭션이 적합합니다.
    public void validatePasswordResetToken(String tokenValue) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (token.isExpired()) {
            // 만료된 토큰은 여기서 삭제하지 않습니다.
            // 실제 비밀번호 재설정 (POST /reset-password) 시에 삭제하는 것이 좋습니다.
            // GET 요청에서는 단순히 만료되었다는 정보만 제공합니다.
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED); // 토큰 만료 시 예외 발생
        }
        // 토큰이 유효하면 아무것도 반환하지 않고 메서드 종료 (컨트롤러로 제어권 반환)
    }

    // 2. 비밀번호 재설정 확인 (토큰 유효성 검증 및 새 비밀번호 설정)
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN)); // 유효하지 않은 토큰

        if (token.isExpired()) {
            passwordResetTokenRepository.delete(token); // 만료된 토큰 삭제
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED); // 토큰 만료
        }

        User user = token.getUser();
        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(newPassword)); // 새 비밀번호 암호화하여 저장
        user.setPasswordChangedAt(LocalDateTime.now()); // 비밀번호 변경 시간 업데이트
        userRepository.save(user); // User 엔티티 저장 (변경 감지로 업데이트될 수도 있음)

        // 토큰 무효화 (사용 완료)
        passwordResetTokenRepository.delete(token);
    }
}
