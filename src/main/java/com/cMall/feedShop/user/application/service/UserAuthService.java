package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager; // AuthenticationManager import 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // UsernamePasswordAuthenticationToken import 추가
import org.springframework.security.core.Authentication; // Authentication import 추가
import org.springframework.security.core.userdetails.UsernameNotFoundException; // UsernameNotFoundException import 추가

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtProvider;
    private final AuthenticationManager authenticationManager; // AuthenticationManager 주입

    /**
     * 사용자 로그인 처리 메서드.
     * 로그인 ID와 비밀번호를 받아 사용자 인증을 수행하고, 성공 시 JWT 토큰을 발급합니다.
     *
     * @param request 사용자 로그인 요청 (로그인 ID, 비밀번호 포함)
     * @return 로그인 응답 (JWT 토큰, 로그인 ID, 사용자 역할 포함)
     * @throws BusinessException 사용자가 존재하지 않거나 비밀번호가 일치하지 않을 경우 발생
     */
    public UserLoginResponse login(UserLoginRequest request) {
        // 1. Spring Security의 AuthenticationManager를 사용하여 인증 시도
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

            // 닉네임 가져오기 (UserProfile이 연관되어 있다면)
            String nickname = null;
            if (user.getUserProfile() != null) {
                nickname = user.getUserProfile().getNickname();
            }

            // 2. 입력된 비밀번호와 저장된 암호화된 비밀번호 비교 (AuthenticationManager가 이미 수행했지만, 명시적으로 다시 확인 가능)
            // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            //     throw new BusinessException(ErrorCode.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
            // }
            // 위 코드는 AuthenticationManager.authenticate()가 이미 처리하므로 불필요합니다.

            // 3. 로그인 성공 시, JWT 토큰 발급
            // generateAccessToken 메서드에 email과 role을 직접 전달합니다.
            String token = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());

            // 4. 로그인 응답 반환
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
