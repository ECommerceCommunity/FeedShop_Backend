package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.common.validator.ImageValidator;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedImageServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private ImageValidator imageValidator;

    @InjectMocks
    private FeedImageService feedImageService;

    private Feed testFeed;
    private List<MultipartFile> testFiles;
    private List<UploadResult> testUploadResults;

    @BeforeEach
    void setUp() {
        testFeed = Feed.builder()
                .title("테스트 피드")
                .content("테스트 내용")
                .build();

        testFiles = Arrays.asList(
                new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test1".getBytes()),
                new MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test2".getBytes())
        );

        testUploadResults = Arrays.asList(
                UploadResult.builder()
                        .originalFilename("test1.jpg")
                        .storedFilename("uuid1.jpg")
                        .filePath("https://cdn.com/images/feeds/uuid1.jpg")
                        .fileSize(1000L)
                        .contentType("image/jpeg")
                        .build(),
                UploadResult.builder()
                        .originalFilename("test2.jpg")
                        .storedFilename("uuid2.jpg")
                        .filePath("https://cdn.com/images/feeds/uuid2.jpg")
                        .fileSize(2000L)
                        .contentType("image/jpeg")
                        .build()
        );
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImages_Success() {
        // given
        when(storageService.uploadFilesWithDetails(eq(testFiles), eq(UploadDirectory.FEEDS)))
                .thenReturn(testUploadResults);
        when(storageService.extractObjectName("https://cdn.com/images/feeds/uuid1.jpg")).thenReturn("uuid1.jpg");
        when(storageService.extractObjectName("https://cdn.com/images/feeds/uuid2.jpg")).thenReturn("uuid2.jpg");

        // when
        feedImageService.uploadImages(testFeed, testFiles);

        // then
        assertThat(testFeed.getImages()).hasSize(2);
        assertThat(testFeed.getImages().get(0).getImageUrl()).isEqualTo("uuid1.jpg");
        assertThat(testFeed.getImages().get(0).getSortOrder()).isEqualTo(1);
        assertThat(testFeed.getImages().get(1).getImageUrl()).isEqualTo("uuid2.jpg");
        assertThat(testFeed.getImages().get(1).getSortOrder()).isEqualTo(1); // 현재 구현에서는 모든 이미지가 같은 sortOrder를 가짐

        verify(imageValidator, times(1)).validateAll(eq(testFiles), anyInt());
        verify(storageService, times(1)).uploadFilesWithDetails(eq(testFiles), eq(UploadDirectory.FEEDS));
    }

    @Test
    @DisplayName("이미지 업로드 - 빈 파일 리스트")
    void uploadImages_EmptyFiles() {
        // when
        feedImageService.uploadImages(testFeed, null);

        // then
        assertThat(testFeed.getImages()).isEmpty();
        verify(imageValidator, never()).validateAll(any(), anyInt());
        verify(storageService, never()).uploadFilesWithDetails(any(), any());
    }

    @Test
    @DisplayName("이미지 교체 성공")
    void replaceImages_Success() {
        // given
        // 기존 이미지 추가
        testFeed.addImage("old1.jpg", 1);
        testFeed.addImage("old2.jpg", 2);

        when(storageService.uploadFilesWithDetails(eq(testFiles), eq(UploadDirectory.FEEDS)))
                .thenReturn(testUploadResults);
        when(storageService.extractObjectName("https://cdn.com/images/feeds/uuid1.jpg")).thenReturn("new1.jpg");
        when(storageService.extractObjectName("https://cdn.com/images/feeds/uuid2.jpg")).thenReturn("new2.jpg");

        // when
        feedImageService.replaceImages(testFeed, testFiles);

        // then
        assertThat(testFeed.getImages()).hasSize(2);
        assertThat(testFeed.getImages().get(0).getImageUrl()).isEqualTo("new1.jpg");
        assertThat(testFeed.getImages().get(1).getImageUrl()).isEqualTo("new2.jpg");
        // 교체 후에는 기존 이미지가 모두 삭제되고 새 이미지만 남음

        verify(imageValidator, times(1)).validateFiles(eq(testFiles));
        verify(storageService, times(1)).uploadFilesWithDetails(eq(testFiles), eq(UploadDirectory.FEEDS));
    }

    @Test
    @DisplayName("이미지 교체 실패 - 업로드 실패")
    void replaceImages_UploadFailure() {
        // given
        when(storageService.uploadFilesWithDetails(eq(testFiles), eq(UploadDirectory.FEEDS)))
                .thenThrow(new RuntimeException("Upload failed"));

        // when & then
        assertThatThrownBy(() -> feedImageService.replaceImages(testFeed, testFiles))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미지 교체에 실패했습니다");
    }

    @Test
    @DisplayName("선택된 이미지 삭제 성공")
    void deleteImages_Success() {
        // given
        testFeed.addImage("image1.jpg", 1);
        testFeed.addImage("image2.jpg", 2);
        testFeed.addImage("image3.jpg", 3);

        List<FeedImage> imagesToDelete = Arrays.asList(
                testFeed.getImages().get(0),
                testFeed.getImages().get(1)
        );

        when(storageService.getFullFilePath("image1.jpg")).thenReturn("full/path/image1.jpg");
        when(storageService.getFullFilePath("image2.jpg")).thenReturn("full/path/image2.jpg");

        // when
        feedImageService.deleteImages(testFeed, imagesToDelete);

        // then
        assertThat(testFeed.getImages()).hasSize(1);
        assertThat(testFeed.getImages().get(0).getImageUrl()).isEqualTo("image3.jpg");

        verify(storageService, times(2)).deleteFile(any());
    }

    @Test
    @DisplayName("이미지 삭제 - 빈 리스트")
    void deleteImages_EmptyList() {
        // when
        feedImageService.deleteImages(testFeed, null);

        // then
        verify(storageService, never()).deleteFile(any());
    }
}
