package com.cMall.feedShop.common.storage;

import com.cMall.feedShop.common.dto.UploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MockStorageService 테스트")
class MockStorageServiceTest {

    private MockStorageService mockStorageService;

    @BeforeEach
    void setUp() {
        mockStorageService = new MockStorageService();
        // 테스트용 CDN URL 설정
        ReflectionTestUtils.setField(mockStorageService, "cdnBaseUrl", "https://mock-cdn.example.com");
    }

    @Test
    @DisplayName("단일 파일 업로드 시 Mock 결과를 반환한다")
    void uploadSingleFile_ReturnsMockResult() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        // when
        List<UploadResult> results = mockStorageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getOriginalFilename()).isEqualTo("mock-file.jpg");
        assertThat(result.getStoredFilename()).isEqualTo("mock-test-image.jpg");
        assertThat(result.getFilePath()).isEqualTo("https://mock-cdn.example.com/images/reviews/test-image.jpg");
        assertThat(result.getFileSize()).isEqualTo(1000L);
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("여러 파일 업로드 시 첫 번째 파일만 Mock 결과를 반환한다")
    void uploadMultipleFiles_ReturnsSingleMockResult() {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "file1",
                "test1.jpg",
                "image/jpeg",
                "test content 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file2",
                "test2.png",
                "image/png",
                "test content 2".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file1, file2);

        // when
        List<UploadResult> results = mockStorageService.uploadFilesWithDetails(files, UploadDirectory.PROFILES);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getOriginalFilename()).isEqualTo("mock-file.jpg");
        assertThat(result.getStoredFilename()).isEqualTo("mock-test1.jpg");
        assertThat(result.getFilePath()).isEqualTo("https://mock-cdn.example.com/images/profiles/test1.jpg");
    }

    @Test
    @DisplayName("파일 삭제 시 항상 true를 반환한다")
    void deleteFile_AlwaysReturnsTrue() {
        // given
        String filePath = "gs://test-bucket/images/reviews/test-file.jpg";

        // when
        boolean result = mockStorageService.deleteFile(filePath);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("null 파일 경로로 삭제 시에도 true를 반환한다")
    void deleteFile_WithNullPath_ReturnsTrue() {
        // when
        boolean result = mockStorageService.deleteFile(null);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("빈 문자열 파일 경로로 삭제 시에도 true를 반환한다")
    void deleteFile_WithEmptyPath_ReturnsTrue() {
        // when
        boolean result = mockStorageService.deleteFile("");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("REVIEWS 디렉토리로 업로드 시 올바른 Mock 결과를 반환한다")
    void uploadToReviewsDirectory_ReturnsCorrectMockResult() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "review-image.jpg",
                "image/jpeg",
                "review content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        // when
        List<UploadResult> results = mockStorageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getFilePath()).contains("mock-cdn.example.com/images/reviews/review-image.jpg");
    }

    @Test
    @DisplayName("PROFILES 디렉토리로 업로드 시 올바른 Mock 결과를 반환한다")
    void uploadToProfilesDirectory_ReturnsCorrectMockResult() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-image.jpg",
                "image/jpeg",
                "profile content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        // when
        List<UploadResult> results = mockStorageService.uploadFilesWithDetails(files, UploadDirectory.PROFILES);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getFilePath()).contains("mock-cdn.example.com/images/profiles/profile-image.jpg");
    }
}
