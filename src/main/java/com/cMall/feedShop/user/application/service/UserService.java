package com.cMall.feedShop.user.application.service;

//import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
//import com.cMall.feedShop.user.application.dto.response.AuthTokenResponse;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor; // Lombok 임포트
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// JWT 토큰 발급/검증을 위한 JwtProvider는 일단 주석 처리 (JWT 도입 전까지)
// import com.cMall.feedShop.user.application.jwt.JwtProvider; // 가상의 JwtProvider

@Service
@Transactional
@RequiredArgsConstructor // final 필드를 인자로 받는 생성자를 자동 생성
public class UserService {

    private final UserRepository userRepository;
    // private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public UserResponse signUp(UserSignUpRequest request) {
        // 1. 중복 체크
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }

        String encodedPasswordFromRequest = request.getPassword();

        // 3. 사용자 생성 및 저장
        User user = new User(
                request.getLoginId(),
                encodedPasswordFromRequest, // 이미 암호화된 비밀번호 사용
                request.getEmail(),
                request.getPhone(),
                UserRole.ROLE_USER
        );
        userRepository.save(user);

        // 4. UserResponse로 변환해서 반환
        return UserResponse.from(user);
    }


//    public AuthTokenResponse login(UserLoginRequest request) {
        // 1. 사용자 검증
//        User user = userRepository.findByUsername(request.getUsername())
//                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 확인
        // Aspect에서 사용한 PasswordEncryptionService의 matches 메서드를 사용해야 함
        // 이를 위해 PasswordEncryptionService를 이 UserService에도 주입받아야 합니다.
        // private final PasswordEncryptionService passwordEncryptionService;
        // if (!passwordEncryptionService.matches(request.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        // }
        // 혹은, 여기에서 Spring Security의 PasswordEncoder를 다시 주입받아 사용할 수도 있습니다.
        // private final PasswordEncoder passwordEncoder;
        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        // }


        // 3. JWT 토큰 생성 (JWT 미도입 시 이 부분은 주석 처리 또는 제거)
        // String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole().name());
        // String refreshToken = jwtProvider.createRefreshToken(user.getUsername());

        // 4. 토큰 반환 (JWT 미도입 시 적절한 응답으로 변경)
        // return new AuthTokenResponse(accessToken, refreshToken);
//        throw new UnsupportedOperationException("JWT is not enabled yet for login."); // 임시
//    }
}