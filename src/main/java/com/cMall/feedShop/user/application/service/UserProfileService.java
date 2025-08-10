package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.ProfileUpdateRequest;
import com.cMall.feedShop.user.application.dto.response.UserProfileResponse;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    public UserProfileService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId)); // 사용자가 없으면 예외 발생

        Optional<UserProfile> userProfileOptional = userProfileRepository.findByUser(user);

        // 3. User 엔티티와 UserProfile 엔티티를 UserProfileResponse DTO로 변환하여 반환
        // UserProfileResponse의 from 메서드를 활용
        return UserProfileResponse.from(user, userProfileOptional.orElse(null));
    }

    public void updateUserProfile(Long userId, String newProfile) {
        log.info("Updating profile for user {} to {}", userId, newProfile); // 플레이스홀더 사용 권장 (성능 및 가독성)
        // ...
        // 만약 여기서 예외를 던지면 @AfterThrowing 또는 @Around의 catch 블록이 동작하는지 확인할 수 있습니다.
        // throw new RuntimeException("Test exception during update");
    }

    @Transactional
    public void updateUserProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserProfile userProfile = user.getUserProfile();

        if (userProfile == null) {
            // 2. 프로필이 없으면 새로 생성합니다.
            // builder를 사용하여 필요한 초기값을 모두 채웁니다.
            userProfile = UserProfile.builder()
                    .user(user) // 양방향 관계 설정을 생성자에서 처리하도록 합니다.
                    .name(request.getName())
                    .nickname(request.getNickname())
                    .phone(request.getPhone())
                    .height(request.getHeight())
                    .footSize(request.getFootSize())
                    .gender(request.getGender())
                    .birthDate(request.getBirthDate())
                    .profileImageUrl(null)
                    .build();
        } else {
            // 3. 프로필이 이미 있으면 업데이트합니다.
            // 객체 내부의 update 메서드를 사용하여 캡슐화를 유지합니다.
            userProfile.updateProfile(
                    request.getName(),
                    request.getNickname(),
                    request.getPhone(),
                    request.getHeight(),
                    request.getFootSize(),
                    request.getGender(),
                    request.getBirthDate()
            );
        }
    }
}
