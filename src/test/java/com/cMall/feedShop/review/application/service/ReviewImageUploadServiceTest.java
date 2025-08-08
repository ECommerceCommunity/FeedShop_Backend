package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.review.infrastructure.config.ReviewImageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewImageUploadService 테스트")
class ReviewImageUploadServiceTest {

    @Mock
    private ReviewImageProperties imageProperties;

    @InjectMocks
    private ReviewImageUploadService uploadService;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("유효한 이미지 파일을 성공적으로 업로드할 수 있다")
    void uploadValidImageFile() throws IOException {
        // given
        setupImagePropertiesForFileUpload();

        MultipartFile validImageFile = mock(MultipartFile.class);
        given(validImageFile.isEmpty()).willReturn(false);
        given(validImageFile.getOriginalFilename()).willReturn("test-image.jpg");
        given(validImageFile.getSize()).willReturn(1024L * 1024L); // 1MB
        given(validImageFile.getContentType()).willReturn("image/jpeg");
        given(validImageFile.getInputStream()).willReturn(new ByteArrayInputStream("test".getBytes()));

        // when
        ReviewImageUploadService.ReviewImageUploadInfo result = uploadService.uploadImage(validImageFile);

        // then
        assertThat(result.getOriginalFilename()).isEqualTo("test-image.jpg");
        assertThat(result.getStoredFilename()).isNotNull();
        assertThat(result.getStoredFilename()).endsWith(".jpg");
        assertThat(result.getFilePath()).isNotNull();
        assertThat(result.getFileSize()).isEqualTo(1024L * 1024L);
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("빈 파일을 업로드하면 예외가 발생한다")
    void uploadEmptyFile() {
        // given
        MultipartFile emptyFile = mock(MultipartFile.class);
        given(emptyFile.isEmpty()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> uploadService.uploadImage(emptyFile))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미지 파일이 필요합니다");
    }

    @Test
    @DisplayName("파일 크기가 제한을 초과하면 예외가 발생한다")
    void uploadOversizedFile() {
        // given
        given(imageProperties.getMaxFileSize()).willReturn(5L * 1024L * 1024L); // 5MB 제한

        MultipartFile oversizedFile = mock(MultipartFile.class);
        given(oversizedFile.isEmpty()).willReturn(false);
        given(oversizedFile.getSize()).willReturn(10L * 1024L * 1024L); // 10MB로 제한 초과

        // when & then
        assertThatThrownBy(() -> uploadService.uploadImage(oversizedFile))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미지 크기는")
                .hasMessageContaining("MB를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("파일명이 null인 경우 예외가 발생한다")
    void uploadFileWithNullFilename() {
        // given
        given(imageProperties.getMaxFileSize()).willReturn(5L * 1024L * 1024L); // 크기 검증 통과용

        MultipartFile nullFilenameFile = mock(MultipartFile.class);
        given(nullFilenameFile.isEmpty()).willReturn(false);
        given(nullFilenameFile.getSize()).willReturn(1024L * 1024L); // 1MB
        given(nullFilenameFile.getOriginalFilename()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> uploadService.uploadImage(nullFilenameFile))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("파일명이 없습니다");
    }

    @Test
    @DisplayName("확장자가 없는 파일명인 경우 예외가 발생한다")
    void uploadFileWithoutExtension() {
        // given
        given(imageProperties.getMaxFileSize()).willReturn(5L * 1024L * 1024L); // 크기 검증 통과용

        MultipartFile noExtensionFile = mock(MultipartFile.class);
        given(noExtensionFile.isEmpty()).willReturn(false);
        given(noExtensionFile.getSize()).willReturn(1024L * 1024L); // 1MB
        given(noExtensionFile.getOriginalFilename()).willReturn("filename_without_extension");

        // when & then
        assertThatThrownBy(() -> uploadService.uploadImage(noExtensionFile))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("파일 확장자가 없습니다");
    }

    @Test
    @DisplayName("허용되지 않는 파일 확장자를 업로드하면 예외가 발생한다")
    void uploadInvalidExtension() {
        // given
        given(imageProperties.getMaxFileSize()).willReturn(5L * 1024L * 1024L); // 크기 검증 통과용
        given(imageProperties.getAllowedExtensions()).willReturn(List.of("jpg", "jpeg", "png", "gif", "webp"));

        MultipartFile invalidImageFile = mock(MultipartFile.class);
        given(invalidImageFile.isEmpty()).willReturn(false);
        given(invalidImageFile.getSize()).willReturn(1024L * 1024L); // 1MB
        given(invalidImageFile.getOriginalFilename()).willReturn("test.txt");

        // when & then
        assertThatThrownBy(() -> uploadService.uploadImage(invalidImageFile))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("지원하지 않는 이미지 형식입니다");
    }

    @Test
    @DisplayName("잘못된 Content-Type을 가진 파일을 업로드하면 예외가 발생한다")
    void uploadInvalidContentType() {
        // given
        given(imageProperties.getMaxFileSize()).willReturn(5L * 1024L * 1024L); // 크기 검증 통과용
        given(imageProperties.getAllowedExtensions()).willReturn(List.of("jpg", "jpeg", "png", "gif", "webp"));
        given(imageProperties.getAllowedContentTypes()).willReturn(List.of("image/jpeg", "image/png", "image/gif", "image/webp"));

        MultipartFile invalidContentTypeFile = mock(MultipartFile.class);
        given(invalidContentTypeFile.isEmpty()).willReturn(false);
        given(invalidContentTypeFile.getSize()).willReturn(1024L * 1024L); // 1MB
        given(invalidContentTypeFile.getOriginalFilename()).willReturn("test.jpg");
        given(invalidContentTypeFile.getContentType()).willReturn("application/octet-stream");

        // when & then
        assertThatThrownBy(() -> uploadService.uploadImage(invalidContentTypeFile))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("올바른 이미지 파일이 아닙니다");
    }

    @Test
    @DisplayName("이미지 개수 제한을 검증할 수 있다")
    void validateImageCount() {
        // given
        given(imageProperties.getMaxImageCount()).willReturn(5);

        int currentCount = 3;
        int newCount = 3; // 총 6개가 되어 제한(5개) 초과

        // when & then
        assertThatThrownBy(() -> uploadService.validateImageCount(currentCount, newCount))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미지는 최대 5개까지만 업로드할 수 있습니다");
    }

    @Test
    @DisplayName("이미지 개수가 제한 내에 있으면 검증을 통과한다")
    void validateImageCountWithinLimit() {
        // given
        given(imageProperties.getMaxImageCount()).willReturn(5);

        int currentCount = 2;
        int newCount = 2; // 총 4개로 제한(5개) 내

        // when & then (예외가 발생하지 않으면 성공)
        uploadService.validateImageCount(currentCount, newCount);
    }

    // 헬퍼 메서드
    private void setupImagePropertiesForFileUpload() {
        given(imageProperties.getUploadPath()).willReturn(tempDir.toString());
        given(imageProperties.getMaxFileSize()).willReturn(5L * 1024L * 1024L);
        given(imageProperties.getAllowedExtensions()).willReturn(List.of("jpg", "jpeg", "png", "gif", "webp"));
        given(imageProperties.getAllowedContentTypes()).willReturn(List.of("image/jpeg", "image/png", "image/gif", "image/webp"));
    }
}