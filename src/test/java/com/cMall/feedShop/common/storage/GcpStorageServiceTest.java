package com.cMall.feedShop.common.storage;

import com.cMall.feedShop.common.dto.UploadResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GcpStorageService 테스트")
class GcpStorageServiceTest {

    @Mock
    private Storage storage;

    @Mock
    private Blob blob;

    @InjectMocks
    private GcpStorageService gcpStorageService;

    private static final String PROJECT_ID = "test-project";
    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gcpStorageService, "projectId", PROJECT_ID);
        ReflectionTestUtils.setField(gcpStorageService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(gcpStorageService, "storage", storage);
    }

    @Test
    @DisplayName("단일 파일 업로드 시 성공적으로 UploadResult를 반환한다")
    void uploadSingleFile_ReturnsUploadResult() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        // when
        List<UploadResult> results = gcpStorageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getOriginalFilename()).isEqualTo("test-image.jpg");
        assertThat(result.getStoredFilename()).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.jpg");
        assertThat(result.getFilePath()).isEqualTo("gs://test-bucket/images/reviews/" + result.getStoredFilename());
        assertThat(result.getFileSize()).isEqualTo(12L);
        assertThat(result.getContentType()).isEqualTo("image/jpeg");

        verify(storage, times(1)).create(any(BlobInfo.class), eq("test content".getBytes()));
    }

    @Test
    @DisplayName("여러 파일 업로드 시 모든 파일의 UploadResult를 반환한다")
    void uploadMultipleFiles_ReturnsAllUploadResults() throws Exception {
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

        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        // when
        List<UploadResult> results = gcpStorageService.uploadFilesWithDetails(files, UploadDirectory.PROFILES);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getOriginalFilename()).isEqualTo("test1.jpg");
        assertThat(results.get(1).getOriginalFilename()).isEqualTo("test2.png");
        assertThat(results.get(0).getFilePath()).contains("images/profiles/");
        assertThat(results.get(1).getFilePath()).contains("images/profiles/");

        verify(storage, times(2)).create(any(BlobInfo.class), any(byte[].class));
    }

    @Test
    @DisplayName("파일 업로드 실패 시 RuntimeException을 던진다")
    void uploadFile_WhenStorageThrowsException_ThrowsRuntimeException() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        when(storage.create(any(BlobInfo.class), any(byte[].class)))
                .thenThrow(new RuntimeException("Storage error"));

        // when & then
        assertThatThrownBy(() -> gcpStorageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 업로드 실패: test-image.jpg");
    }

    @Test
    @DisplayName("파일 삭제 성공 시 true를 반환한다")
    void deleteFile_WhenSuccessful_ReturnsTrue() {
        // given
        String filePath = "gs://test-bucket/images/reviews/test-file.jpg";
        when(storage.delete(any(BlobId.class))).thenReturn(true);

        // when
        boolean result = gcpStorageService.deleteFile(filePath);

        // then
        assertThat(result).isTrue();
        verify(storage, times(1)).delete(BlobId.of(BUCKET_NAME, "images/reviews/test-file.jpg"));
    }

    @Test
    @DisplayName("파일 삭제 실패 시 false를 반환한다")
    void deleteFile_WhenUnsuccessful_ReturnsFalse() {
        // given
        String filePath = "gs://test-bucket/images/reviews/test-file.jpg";
        when(storage.delete(any(BlobId.class))).thenReturn(false);

        // when
        boolean result = gcpStorageService.deleteFile(filePath);

        // then
        assertThat(result).isFalse();
        verify(storage, times(1)).delete(BlobId.of(BUCKET_NAME, "images/reviews/test-file.jpg"));
    }

    @Test
    @DisplayName("잘못된 파일 경로로 삭제 시 false를 반환한다")
    void deleteFile_WithInvalidPath_ReturnsFalse() {
        // given
        String invalidPath = "invalid-path";

        // when
        boolean result = gcpStorageService.deleteFile(invalidPath);

        // then
        assertThat(result).isFalse();
        verify(storage, never()).delete(any(BlobId.class));
    }

    @Test
    @DisplayName("다른 버킷의 파일 경로로 삭제 시 false를 반환한다")
    void deleteFile_WithDifferentBucket_ReturnsFalse() {
        // given
        String differentBucketPath = "gs://different-bucket/images/reviews/test-file.jpg";

        // when
        boolean result = gcpStorageService.deleteFile(differentBucketPath);

        // then
        assertThat(result).isFalse();
        verify(storage, never()).delete(any(BlobId.class));
    }

    @Test
    @DisplayName("파일 삭제 중 예외 발생 시 false를 반환한다")
    void deleteFile_WhenExceptionOccurs_ReturnsFalse() {
        // given
        String filePath = "gs://test-bucket/images/reviews/test-file.jpg";
        when(storage.delete(any(BlobId.class))).thenThrow(new RuntimeException("Storage error"));

        // when
        boolean result = gcpStorageService.deleteFile(filePath);

        // then
        assertThat(result).isFalse();
        verify(storage, times(1)).delete(any(BlobId.class));
    }

    @Test
    @DisplayName("null 파일 경로로 삭제 시 false를 반환한다")
    void deleteFile_WithNullPath_ReturnsFalse() {
        // when
        boolean result = gcpStorageService.deleteFile(null);

        // then
        assertThat(result).isFalse();
        verify(storage, never()).delete(any(BlobId.class));
    }

    @Test
    @DisplayName("REVIEWS 디렉토리로 업로드 시 올바른 경로에 저장된다")
    void uploadToReviewsDirectory_StoresInCorrectPath() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "review-image.jpg",
                "image/jpeg",
                "review content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        // when
        List<UploadResult> results = gcpStorageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFilePath()).contains("images/reviews/");
    }

    @Test
    @DisplayName("PROFILES 디렉토리로 업로드 시 올바른 경로에 저장된다")
    void uploadToProfilesDirectory_StoresInCorrectPath() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-image.jpg",
                "image/jpeg",
                "profile content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        // when
        List<UploadResult> results = gcpStorageService.uploadFilesWithDetails(files, UploadDirectory.PROFILES);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFilePath()).contains("images/profiles/");
    }

    @Test
    @DisplayName("파일 확장자가 없는 파일도 정상적으로 처리된다")
    void uploadFile_WithoutExtension_HandlesCorrectly() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testfile",
                "text/plain",
                "test content".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file);

        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        // when
        List<UploadResult> results = gcpStorageService.uploadFilesWithDetails(files, UploadDirectory.REVIEWS);

        // then
        assertThat(results).hasSize(1);
        UploadResult result = results.get(0);
        assertThat(result.getStoredFilename()).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$");
        assertThat(result.getStoredFilename()).doesNotContain(".");
    }
}
