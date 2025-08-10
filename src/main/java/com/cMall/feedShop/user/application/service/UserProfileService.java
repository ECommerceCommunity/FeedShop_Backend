package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.UserProfileResponse;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Optional<UserProfile> userProfileOptional = userProfileRepository.findByUser(user);

        return UserProfileResponse.from(user, userProfileOptional.orElse(null));
    }

    public void updateUserProfile(Long userId, String newProfile) {
        log.info("Updating profile for user {} to {}", userId, newProfile); // 플레이스홀더 사용 권장 (성능 및 가독성)
        // ...
        // 만약 여기서 예외를 던지면 @AfterThrowing 또는 @Around의 catch 블록이 동작하는지 확인할 수 있습니다.
        // throw new RuntimeException("Test exception during update");
    }
}
