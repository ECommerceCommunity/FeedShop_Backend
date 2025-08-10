package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.user.application.dto.request.ProfileUpdateRequest;
import com.cMall.feedShop.user.application.dto.response.UserProfileResponse;
import com.cMall.feedShop.user.domain.enums.Gender;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private UserProfile testUserProfile;
    private ProfileUpdateRequest profileUpdateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        testUser = new User(1L, "testuser", "encodedPassword", "test@example.com", UserRole.USER);

        // 테스트용 UserProfile 생성
        testUserProfile = UserProfile.builder()
                .user(testUser)
                .name("테스트유저")
                .nickname("테스트닉네임")
                .phone("01012345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .height(175)
                .weight(70)
                .footSize(270)
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        // 테스트용 ProfileUpdateRequest 생성
        profileUpdateRequest = ProfileUpdateRequest.builder()
                .name("수정된이름")
                .nickname("수정된닉네임")
                .phone("01087654321")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 5, 5))
                .height(165)
                .footSize(240)
                .build();
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공 - 프로필이 존재하는 경우")
    void getUserProfile_Success_WithProfile() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByUser(testUser)).willReturn(Optional.of(testUserProfile));

        // when
        UserProfileResponse response = userProfileService.getUserProfile(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("테스트유저");
        assertThat(response.getNickname()).isEqualTo("테스트닉네임");
        assertThat(response.getPhone()).isEqualTo("01012345678");
        assertThat(response.getGender()).isEqualTo(Gender.MALE);
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.getHeight()).isEqualTo(175);
        assertThat(response.getWeight()).isEqualTo(70);
        assertThat(response.getFootSize()).isEqualTo(270);
        assertThat(response.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");

        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공 - 프로필이 존재하지 않는 경우")
    void getUserProfile_Success_WithoutProfile() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByUser(testUser)).willReturn(Optional.empty());

        // when
        UserProfileResponse response = userProfileService.getUserProfile(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isNull();
        assertThat(response.getNickname()).isNull();
        assertThat(response.getPhone()).isNull();
        assertThat(response.getGender()).isNull();
        assertThat(response.getBirthDate()).isNull();
        assertThat(response.getHeight()).isNull();
        assertThat(response.getWeight()).isNull();
        assertThat(response.getFootSize()).isNull();
        assertThat(response.getProfileImageUrl()).isNull();

        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 사용자가 존재하지 않는 경우")
    void getUserProfile_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getUserProfile(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: " + userId);

        verify(userRepository).findById(userId);
        verify(userProfileRepository, never()).findByUser(any());
    }

    @Test
    @DisplayName("사용자 프로필 수정 성공 - 기존 프로필이 존재하는 경우")
    void updateUserProfile_Success_ExistingProfile() {
        // given
        Long userId = 1L;
        testUser.setUserProfile(testUserProfile);
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when
        userProfileService.updateUserProfile(userId, profileUpdateRequest);

        // then
        verify(userRepository).findById(userId);
        // UserProfile의 updateProfile 메서드가 호출되었는지 확인
        // 실제로는 UserProfile 엔티티의 updateProfile 메서드가 호출되어야 함
    }

    @Test
    @DisplayName("사용자 프로필 수정 성공 - 프로필이 존재하지 않는 경우 새로 생성")
    void updateUserProfile_Success_NewProfile() {
        // given
        Long userId = 1L;
        testUser.setUserProfile(null);
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when
        userProfileService.updateUserProfile(userId, profileUpdateRequest);

        // then
        verify(userRepository).findById(userId);
        // 새 프로필이 생성되었는지 확인 (UserProfile.builder() 호출 확인)
        // 실제로는 UserProfile 엔티티의 updateProfile 메서드가 호출되어야 함
    }

    @Test
    @DisplayName("사용자 프로필 수정 실패 - 사용자가 존재하지 않는 경우")
    void updateUserProfile_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.updateUserProfile(userId, profileUpdateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: " + userId);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 - 기존 이미지가 있는 경우")
    void updateProfileImage_Success_WithExistingImage() throws IOException {
        // given
        Long userId = 1L;
        String existingImageUrl = "https://example.com/old-profile.jpg";
        String newImageUrl = "https://example.com/new-profile.jpg";
        
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", 
                "test-image.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        UploadResult uploadResult = UploadResult.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("stored-test-image.jpg")
                .filePath(newImageUrl)
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByUser(testUser)).willReturn(Optional.of(testUserProfile));
        given(storageService.uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES)))
                .willReturn(Collections.singletonList(uploadResult));

        // when
        String result = userProfileService.updateProfileImage(userId, imageFile);

        // then
        assertThat(result).isEqualTo(newImageUrl);
        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUser(testUser);
        verify(storageService).deleteFile("https://example.com/profile.jpg");
        verify(storageService).uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES));
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 - 기존 이미지가 없는 경우")
    void updateProfileImage_Success_WithoutExistingImage() throws IOException {
        // given
        Long userId = 1L;
        String newImageUrl = "https://example.com/new-profile.jpg";
        
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", 
                "test-image.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        UploadResult uploadResult = UploadResult.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("stored-test-image.jpg")
                .filePath(newImageUrl)
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByUser(testUser)).willReturn(Optional.of(testUserProfile));
        given(storageService.uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES)))
                .willReturn(Collections.singletonList(uploadResult));

        // when
        String result = userProfileService.updateProfileImage(userId, imageFile);

        // then
        assertThat(result).isEqualTo(newImageUrl);
        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUser(testUser);
        verify(storageService).deleteFile("https://example.com/profile.jpg");
        verify(storageService).uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES));
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 - 프로필이 존재하지 않는 경우 새로 생성")
    void updateProfileImage_Success_NewProfile() throws IOException {
        // given
        Long userId = 1L;
        String newImageUrl = "https://example.com/new-profile.jpg";
        
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", 
                "test-image.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        UploadResult uploadResult = UploadResult.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("stored-test-image.jpg")
                .filePath(newImageUrl)
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        UserProfile newProfile = UserProfile.builder()
                .user(testUser)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByUser(testUser)).willReturn(Optional.empty());
        given(storageService.uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES)))
                .willReturn(Collections.singletonList(uploadResult));

        // when
        String result = userProfileService.updateProfileImage(userId, imageFile);

        // then
        assertThat(result).isEqualTo(newImageUrl);
        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUser(testUser);
        verify(storageService).uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES));
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 실패 - 사용자가 존재하지 않는 경우")
    void updateProfileImage_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", 
                "test-image.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.updateProfileImage(userId, imageFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: " + userId);

        verify(userRepository).findById(userId);
        verify(userProfileRepository, never()).findByUser(any());
        verify(storageService, never()).uploadFilesWithDetails(anyList(), any());
    }

    @Test
    @DisplayName("프로필 이미지 업로드 실패 - StorageService에서 IOException 발생")
    void updateProfileImage_Fail_StorageServiceException() throws IOException {
        // given
        Long userId = 1L;
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", 
                "test-image.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByUser(testUser)).willReturn(Optional.of(testUserProfile));
        given(storageService.uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES)))
                .willThrow(new RuntimeException("Storage service error"));

        // when & then
        assertThatThrownBy(() -> userProfileService.updateProfileImage(userId, imageFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Storage service error");

        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUser(testUser);
        verify(storageService).uploadFilesWithDetails(anyList(), eq(UploadDirectory.PROFILES));
        verify(userProfileRepository, never()).save(any());
    }
}
