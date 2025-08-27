package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
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
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return UserProfileResponse.from(user, user.getUserProfile());
    }

    @Transactional
    public void updateUserProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserProfile userProfile = user.getUserProfile();

        if (userProfile == null) {
            userProfile = UserProfile.builder()
                    .user(user)
                    .name(request.getName())
                    .nickname(request.getNickname())
                    .phone(request.getPhone())
                    .height(request.getHeight())
                    .weight(request.getWeight())
                    .footSize(request.getFootSize())
                    .footWidth(request.getFootWidth())
                    .footArchType(request.getFootArchType())
                    .gender(request.getGender())
                    .birthDate(request.getBirthDate())
                    .profileImageUrl(null)
                    .build();
            
            user.setUserProfile(userProfile);
            userProfileRepository.save(userProfile);
        } else {
            userProfile.updateProfile(
                    request.getName(),
                    request.getNickname(),
                    request.getPhone(),
                    request.getHeight(),
                    request.getWeight(),
                    request.getFootSize(),
                    request.getFootWidth(),
                    request.getFootArchType(),
                    request.getGender(),
                    request.getBirthDate()
            );
            
            userProfileRepository.save(userProfile);
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

                UploadResult uploadResult = storageService.uploadFilesWithDetails(Collections.singletonList(image), UploadDirectory.PROFILES).get(0);
        userProfile.updateProfileImageUrl(uploadResult.getFilePath());

        userProfileRepository.save(userProfile);
        return uploadResult.getFilePath();
    }
}
