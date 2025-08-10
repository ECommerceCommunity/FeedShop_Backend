package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.user.application.dto.request.ProfileUpdateRequest;
import com.cMall.feedShop.user.application.dto.response.UserProfileResponse;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final StorageService storageService;

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Optional<UserProfile> userProfileOptional = userProfileRepository.findByUser(user);

        return UserProfileResponse.from(user, userProfileOptional.orElse(null));
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

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .build();
                    newProfile.setUser(user);
                    return newProfile;
                });

        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
            storageService.deleteFile(userProfile.getProfileImageUrl());
        }

        UploadResult uploadResult = storageService.uploadFilesWithDetails(Collections.singletonList(image), "profile-images").get(0);
        userProfile.updateProfileImageUrl(uploadResult.getFilePath());

        userProfileRepository.save(userProfile);
        return uploadResult.getFilePath();
    }
}
