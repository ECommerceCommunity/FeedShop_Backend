package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.service.EmailService;
import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.exception.AccountNotVerifiedException;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.exception.UserNotFoundException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.cMall.feedShop.common.exception.ErrorCode.*;
import static com.cMall.feedShop.common.exception.ErrorCode.VERIFICATION_TOKEN_EXPIRED;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.verification-url}")
    private String verificationUrl;

    public UserResponse signUp(UserSignUpRequest request) {
        // 이메일 중복 확인 및 상태에 따른 처리
        Optional<User> existingUserOptional = userRepository.findByEmail(request.getEmail());

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();

            if (existingUser.getStatus() == UserStatus.ACTIVE) {
                // 이미 활성(ACTIVE) 상태의 사용자가 해당 이메일로 가입되어 있다면
                throw new UserException(DUPLICATE_EMAIL); // ErrorCode 사용
            } else if (existingUser.getStatus() == UserStatus.PENDING) {
                // PENDING 상태의 사용자가 존재한다면 (이메일 인증 미완료)
                updateVerificationToken(existingUser); // private 메서드로 분리된 로직 사용

                userRepository.save(existingUser);

                sendVerificationEmail(existingUser, "회원가입 재인증을 완료해주세요.", "회원가입 재인증을 요청하셨습니다. 아래 링크를 클릭하여 이메일 인증을 완료해주세요:");
                // 재인증 메일 발송 후 예외 처리 (DUPLICATE_EMAIL과 함께 메시지 전달)
                throw new UserException(DUPLICATE_EMAIL, "재인증 메일이 발송되었습니다. 메일을 확인하여 인증을 완료해주세요.");
            }
            // 기타 다른 상태 (DELETED 등)에 대한 처리도 추가할 수 있습니다.
        }

        // loginId 자동 생성 (UUID 사용)
        String generatedLoginId = UUID.randomUUID().toString();

        // 비밀번호 암호화
        String finalPasswordToSave;
        if (request.getPassword().startsWith("$2a$") || request.getPassword().startsWith("$2b$") || request.getPassword().startsWith("$2y$")) {
            finalPasswordToSave = request.getPassword();
        } else {
            finalPasswordToSave = passwordEncoder.encode(request.getPassword());
        }

        // User 엔티티 생성 및 초기화
        User user = new User(
                generatedLoginId,
                finalPasswordToSave,
                request.getEmail(),
                UserRole.USER
        );
        user.setStatus(UserStatus.PENDING);
        user.setPasswordChangedAt(LocalDateTime.now());

        // 개발 브랜치에서 분리된 updateVerificationToken 메서드 사용
        updateVerificationToken(user);

        UserProfile userProfile = new UserProfile(
                user,
                request.getName(),
                request.getName(),
                request.getPhone()
        );

        user.setUserProfile(userProfile);

        // develop 브랜치의 createUser에서 반환하는 방식과 동일하게 변경
        userRepository.save(user); // 저장 후 반환
        sendVerificationEmail(user, "회원가입을 완료해주세요.", "cMall 회원가입을 환영합니다. 아래 링크를 클릭하여 이메일 인증을 완료해주세요:");

        return UserResponse.from(user);
    }

    // 이메일 인증 토큰 업데이트 로직
    public void updateVerificationToken(User user) {
        String newVerificationToken = UUID.randomUUID().toString();
        LocalDateTime newExpiryTime = LocalDateTime.now().plusHours(1);
        user.setVerificationToken(newVerificationToken);
        user.setVerificationTokenExpiry(newExpiryTime);
        // 이 메서드 내에서 save를 호출하지 않고, 호출하는 쪽에서 save하도록 하는 것이 트랜잭션 관리에 더 유연할 수 있습니다.
        // 현재 signUp 메서드에서 save를 하고 있으므로 여기서는 save를 제거합니다.
        // userRepository.save(user); // 이 부분은 호출하는 곳에서 처리
    }


    public void sendVerificationEmail(User user, String subject, String contentBody) {
        String verificationLink = verificationUrl + user.getVerificationToken();
        String emailSubject = "[cMall] " + subject;
        String emailContent = "안녕하세요, " + user.getUserProfile().getName() + "!\n\n" +
                contentBody + "\n\n" +
                verificationLink + "\n\n" +
                "본 링크는 1시간 후 만료됩니다.\n" +
                "감사합니다.\ncMall 팀 드림";

        emailService.sendSimpleEmail(user.getEmail(), emailSubject, emailContent);
    }

    // 아이디 중복 확인 메서드 (API 제공 시 활용)
    @Transactional(readOnly = true)
    public boolean isLoginIdDuplicated(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }


    // 이메일 중복 확인 메서드 (API 제공 시 활용)
    @Transactional(readOnly = true)
    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email); // User Repository 사용
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new UserException(INVALID_VERIFICATION_TOKEN));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new UserException(ACCOUNT_ALREADY_VERIFIED);
        }

        // 토큰 만료 확인 전에 토큰이 일치하는지 확인하는 로직은 findByVerificationToken에서 이미 처리되므로 제거
        // if (user.getVerificationToken() == null || !user.getVerificationToken().equals(token)) {
        //     throw new UserException(INVALID_VERIFICATION_TOKEN); // 이 예외는 findByVerificationToken에서 던져짐
        // }

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            userRepository.save(user); // 만료된 토큰 정보 초기화 저장
            throw new UserException(VERIFICATION_TOKEN_EXPIRED);
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user); // 최종 상태 변경 및 토큰 초기화 저장
    }

    public void deleteUser(User user) {
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserException(USER_ALREADY_DELETED); // 이미 탈퇴된 계정 예외
        }
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        // TODO: 사용자와 관련된 다른 데이터 (주문, 게시글, 댓글 등) 처리 로직 추가
        // - 해당 사용자의 모든 게시글/댓글을 삭제 (Hard Delete) 또는 작성자를 '탈퇴한 사용자' 등으로 변경 (Soft Delete)
        // - 해당 사용자의 주문 내역은 유지하되, 사용자 정보는 비식별화 (개인정보보호)
        // - 예시: orderService.anonymizeUserOrders(userId);
        // - 예시: boardService.updateAuthorToWithdrawn(userId);
    }

    // 관리자 권한 확인 로직
    public void checkAdminAuthority(String methodName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .noneMatch(role -> role.equals("ROLE_ADMIN"))) {
            String requester = (authentication != null) ? authentication.getName() : "anonymous";
            log.warn("Unauthorized access attempt to '{}' by user '{}'", methodName, requester);
            throw new UserException(FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    // 1. 사용자 ID로 회원 탈퇴 (관리자용 또는 내부 로직)
    public void withdrawUser(Long userId) {
        checkAdminAuthority("withdrawUser"); // 관리자 권한 확인 추가
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        deleteUser(user);
    }

    // 2. 관리자용: 이메일로 사용자 탈퇴 (비밀번호 확인 불필요)
    public void adminWithdrawUserByEmail(String email) {
        checkAdminAuthority("adminWithdrawUserByEmail"); // 관리자 권한 확인 추가
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND, "사용자를 찾을 수 없습니다. 이메일: " + email)); // UserException 사용
        deleteUser(user);
    }

    // 3. 사용자용: 이메일과 비밀번호 확인으로 회원 탈퇴 (보안 강화)
    public void withdrawCurrentUserWithPassword(String email, String rawPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserException(UNAUTHORIZED, "로그인된 사용자만 탈퇴할 수 있습니다.");
        }
        String currentLoggedInUserEmail = authentication.getName();
        if (!currentLoggedInUserEmail.equals(email)) {
            log.warn("Forbidden withdrawal attempt: User '{}' tried to delete account of '{}'.", currentLoggedInUserEmail, email); // 로그 추가
            throw new UserException(FORBIDDEN, "다른 사용자의 계정을 탈퇴할 수 없습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND, "사용자를 찾을 수 없습니다. 이메일: " + email)); // UserException 사용

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new UserException(INVALID_PASSWORD);
        }

        deleteUser(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findByUsernameAndPhoneNumber(String username, String phoneNumber) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(INVALID_INPUT_VALUE, "이름을 입력해주세요.");
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new BusinessException(INVALID_INPUT_VALUE, "전화번호를 입력해주세요.");
        }

        // 사용자 조회
        User user = userRepository.findByUserProfile_NameAndUserProfile_Phone(username.trim(), phoneNumber.trim())
                .orElseThrow(() -> new UserNotFoundException("입력하신 정보와 일치하는 사용자를 찾을 수 없습니다."));

        // 계정 상태 확인
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(USER_ALREADY_DELETED, "탈퇴된 계정입니다.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new AccountNotVerifiedException();
        }

        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUserProfile().getName())
                .email(maskEmail(user.getEmail()))
                .phone(user.getUserProfile().getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .message("계정 정보를 성공적으로 찾았습니다.")
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "*@" + domain;
        } else {
            return localPart.charAt(0) + "*".repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1) + "@" + domain;
        }
    }
}
