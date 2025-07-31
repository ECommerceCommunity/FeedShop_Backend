package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.domain.exception.AccountNotVerifiedException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public interface UserAuthService {
    UserLoginResponse login(UserLoginRequest request);

    void requestPasswordReset(String email);

    void validatePasswordResetToken(String tokenValue);

    void resetPassword(String tokenValue, String newPassword);

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
    // 기타 인증 관련 메서드 (예: 회원가입, 비밀번호 재설정 등)를 여기에 추가.
}
