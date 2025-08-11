package com.cMall.feedShop.common.storage;

import com.cMall.feedShop.common.dto.UploadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create"
})
@DisplayName("StorageService 통합 테스트")
class StorageServiceIntegrationTest {

    @Autowired
    private StorageService storageService;

    @Test
    @DisplayName("test 프로파일에서 MockStorageService가 주입된다")
    void testProfile_UsesMockStorageService() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        // when
        List<UploadResult> results = storageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getOriginalFilename()).isEqualTo("mock-file.jpg");
        assertThat(result.getStoredFilename()).isEqualTo("mock-test-image.jpg");
        assertThat(result.getFilePath()).isEqualTo("https://mock-gcp-bucket/mock-path/test-image.jpg");
        assertThat(result.getFileSize()).isEqualTo(1000L);
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("test 프로파일에서 파일 삭제가 항상 성공한다")
    void testProfile_DeleteFileAlwaysSucceeds() {
        // given
        String filePath = "gs://test-bucket/images/reviews/test-file.jpg";

        // when
        boolean result = storageService.deleteFile(filePath);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("REVIEWS 디렉토리로 업로드 시 Mock 결과가 올바르게 반환된다")
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
        List<UploadResult> results = storageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getFilePath()).contains("mock-gcp-bucket/mock-path/review-image.jpg");
    }

    @Test
    @DisplayName("PROFILES 디렉토리로 업로드 시 Mock 결과가 올바르게 반환된다")
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
        List<UploadResult> results = storageService.uploadFilesWithDetails(files, UploadDirectory.PROFILES);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getFilePath()).contains("mock-gcp-bucket/mock-path/profile-image.jpg");
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
        List<UploadResult> results = storageService.uploadFilesWithDetails(files, UploadDirectory.PROFILES);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getOriginalFilename()).isEqualTo("mock-file.jpg");
        assertThat(result.getStoredFilename()).isEqualTo("mock-test1.jpg");
        assertThat(result.getFilePath()).isEqualTo("https://mock-gcp-bucket/mock-path/test1.jpg");
    }

    @Test
    @DisplayName("null 파일 경로로 삭제 시에도 true를 반환한다")
    void deleteFile_WithNullPath_ReturnsTrue() {
        // when
        boolean result = storageService.deleteFile(null);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("빈 문자열 파일 경로로 삭제 시에도 true를 반환한다")
    void deleteFile_WithEmptyPath_ReturnsTrue() {
        // when
        boolean result = storageService.deleteFile("");

        // then
        assertThat(result).isTrue();
    }
}
