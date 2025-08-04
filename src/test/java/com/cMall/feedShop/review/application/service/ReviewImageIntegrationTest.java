package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.infrastructure.config.ReviewImageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewImage 통합 테스트")
class ReviewImageIntegrationTest {

    @TempDir
    Path tempDir;

    private ReviewImageUploadService uploadService;
    private ReviewImageProperties imageProperties;

    @BeforeEach
    void setUp() {
        imageProperties = new ReviewImageProperties();
        imageProperties.setUploadPath(tempDir.toString());
        imageProperties.setMaxFileSize(5L * 1024L * 1024L); // 5MB
        imageProperties.setMaxImageCount(5);
        imageProperties.setAllowedExtensions(List.of("jpg", "jpeg", "png", "gif", "webp"));
        imageProperties.setAllowedContentTypes(
                List.of("image/jpeg", "image/png", "image/gif", "image/webp"));

        uploadService = new ReviewImageUploadService(imageProperties);
    }

    @Test
    @DisplayName("실제 파일 업로드와 삭제가 정상 동작한다")
    void uploadAndDeleteActualFile() throws IOException {
        // given
        byte[] imageContent = createTestImageContent();
        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                imageContent
        );

        // when - 업로드
        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo = uploadService.uploadImage(imageFile);

        // then - 업로드 검증
        assertThat(uploadInfo.getOriginalFilename()).isEqualTo("test-image.jpg");
        assertThat(uploadInfo.getStoredFilename()).endsWith(".jpg");
        assertThat(uploadInfo.getFilePath()).isNotNull();
        assertThat(uploadInfo.getFileSize()).isEqualTo(imageContent.length);
        assertThat(uploadInfo.getContentType()).isEqualTo("image/jpeg");

        // 실제 파일이 생성되었는지 확인
        Path uploadedFile = Paths.get(tempDir.toString(), uploadInfo.getFilePath());
        assertThat(Files.exists(uploadedFile)).isTrue();
        assertThat(Files.size(uploadedFile)).isEqualTo(imageContent.length);

        // when - 삭제
        uploadService.deleteImage(uploadInfo.getFilePath());

        // then - 삭제 검증
        assertThat(Files.exists(uploadedFile)).isFalse();
    }

    @Test
    @DisplayName("날짜별 디렉토리가 자동으로 생성된다")
    void createDateDirectory() throws IOException {
        // given
        byte[] imageContent = createTestImageContent();
        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.png",
                "image/png",
                imageContent
        );

        // when
        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo = uploadService.uploadImage(imageFile);

        // then
        String filePath = uploadInfo.getFilePath();
        assertThat(filePath).matches("\\d{4}/\\d{2}/\\d{2}/.+\\.png"); // yyyy/MM/dd/filename.png 패턴

        // 디렉토리가 실제로 생성되었는지 확인
        Path directoryPath = Paths.get(tempDir.toString(), filePath).getParent();
        assertThat(Files.exists(directoryPath)).isTrue();
        assertThat(Files.isDirectory(directoryPath)).isTrue();
    }

    @Test
    @DisplayName("동일한 원본 파일명이라도 서로 다른 저장 파일명이 생성된다")
    void generateUniqueStoredFilenames() throws IOException {
        // given
        byte[] imageContent1 = createTestImageContent();
        byte[] imageContent2 = createTestImageContent();

        MultipartFile imageFile1 = new MockMultipartFile(
                "image1", "same-name.jpg", "image/jpeg", imageContent1
        );
        MultipartFile imageFile2 = new MockMultipartFile(
                "image2", "same-name.jpg", "image/jpeg", imageContent2
        );

        // when
        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo1 = uploadService.uploadImage(imageFile1);
        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo2 = uploadService.uploadImage(imageFile2);

        // then
        assertThat(uploadInfo1.getOriginalFilename()).isEqualTo("same-name.jpg");
        assertThat(uploadInfo2.getOriginalFilename()).isEqualTo("same-name.jpg");
        assertThat(uploadInfo1.getStoredFilename()).isNotEqualTo(uploadInfo2.getStoredFilename());

        // 두 파일 모두 실제로 저장되었는지 확인
        Path file1 = Paths.get(tempDir.toString(), uploadInfo1.getFilePath());
        Path file2 = Paths.get(tempDir.toString(), uploadInfo2.getFilePath());
        assertThat(Files.exists(file1)).isTrue();
        assertThat(Files.exists(file2)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 파일을 삭제해도 예외가 발생하지 않는다")
    void deleteNonExistentFile() {
        // given
        String nonExistentFilePath = "2024/01/01/non-existent-file.jpg";

        // when & then (예외가 발생하지 않아야 함)
        uploadService.deleteImage(nonExistentFilePath);
    }

    private byte[] createTestImageContent() {
        // 간단한 테스트용 이미지 데이터 (실제로는 더 복잡한 이미지 바이너리)
        return "fake-image-content-for-testing".getBytes();
    }
}