package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.application.dto.response.ReviewImageResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.infrastructure.config.ReviewImageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewImageService 테스트")
class ReviewImageServiceTest {

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private ReviewImageUploadService uploadService;

    @Mock
    private ReviewImageProperties imageProperties;

    @InjectMocks
    private ReviewImageService reviewImageService;

    private Review testReview;
    private ReviewImage testReviewImage;

    @BeforeEach
    void setUp() {
        // Mock 객체 생성만 하고 stubbing은 각 테스트에서 개별 설정
        testReview = mock(Review.class);
        testReviewImage = mock(ReviewImage.class);
    }

    @Test
    @DisplayName("리뷰 이미지를 성공적으로 저장할 수 있다")
    void saveReviewImages() throws IOException {
        // given - 이 테스트에서만 필요한 모든 stubbing
        given(testReview.getReviewId()).willReturn(1L);

        MultipartFile testImageFile = createTestImageFile("test-image.jpg");
        List<MultipartFile> imageFiles = List.of(testImageFile);

        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo =
                ReviewImageUploadService.ReviewImageUploadInfo.builder()
                        .originalFilename("test-image.jpg")
                        .storedFilename("uuid-filename.jpg")
                        .filePath("2024/01/01/uuid-filename.jpg")
                        .fileSize(1024L)
                        .contentType("image/jpeg")
                        .build();

        given(reviewImageRepository.countActiveImagesByReviewId(1L)).willReturn(0L);
        given(uploadService.uploadImage(testImageFile)).willReturn(uploadInfo);
        given(reviewImageRepository.save(any(ReviewImage.class))).willReturn(testReviewImage);

        // when
        List<ReviewImage> result = reviewImageService.saveReviewImages(testReview, imageFiles);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testReviewImage);
        verify(uploadService, times(1)).validateImageCount(0, 1);
        verify(uploadService, times(1)).uploadImage(testImageFile);
        verify(reviewImageRepository, times(1)).save(any(ReviewImage.class));
    }

    @Test
    @DisplayName("빈 이미지 리스트를 전달하면 빈 리스트를 반환한다")
    void saveReviewImagesWithEmptyList() {
        // given - 이 테스트는 stubbing 없이도 동작
        List<MultipartFile> emptyImageFiles = List.of();

        // when
        List<ReviewImage> result = reviewImageService.saveReviewImages(testReview, emptyImageFiles);

        // then
        assertThat(result).isEmpty();
        verify(uploadService, never()).uploadImage(any());
        verify(reviewImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("null 이미지 리스트를 전달하면 빈 리스트를 반환한다")
    void saveReviewImagesWithNullList() {
        // given - 이 테스트는 stubbing 없이도 동작

        // when
        List<ReviewImage> result = reviewImageService.saveReviewImages(testReview, null);

        // then
        assertThat(result).isEmpty();
        verify(uploadService, never()).uploadImage(any());
        verify(reviewImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("리뷰 이미지를 성공적으로 삭제할 수 있다")
    void deleteReviewImages() {
        // given - 이 테스트에서만 필요한 stubbing
        List<ReviewImage> images = List.of(testReviewImage);
        given(reviewImageRepository.findActiveImagesByReviewId(1L)).willReturn(images);
        given(testReviewImage.getFilePath()).willReturn("2024/01/01/test-image.jpg");

        // when
        reviewImageService.deleteReviewImages(1L);

        // then
        verify(testReviewImage, times(1)).delete();
        verify(uploadService, times(1)).deleteImage("2024/01/01/test-image.jpg");
    }

    @Test
    @DisplayName("리뷰 이미지 목록을 성공적으로 조회할 수 있다")
    void getReviewImages() {
        // given - 이 테스트에서만 필요한 모든 stubbing
        List<ReviewImage> images = List.of(testReviewImage);
        given(reviewImageRepository.findActiveImagesByReviewId(1L))
                .willReturn(images);
        given(testReviewImage.getReviewImageId()).willReturn(1L);
        given(testReviewImage.getOriginalFilename()).willReturn("test-image.jpg");
        given(testReviewImage.getImageOrder()).willReturn(1);
        given(testReviewImage.getFileSize()).willReturn(1024L);
        given(testReviewImage.getFullImageUrl("http://localhost:8080"))
                .willReturn("http://localhost:8080/uploads/reviews/2024/01/01/uuid-filename.jpg");
        given(imageProperties.getBaseUrl()).willReturn("http://localhost:8080");

        // when
        List<ReviewImageResponse> result = reviewImageService.getReviewImages(1L);

        // then
        assertThat(result).hasSize(1);
        ReviewImageResponse response = result.get(0);
        assertThat(response.getReviewImageId()).isEqualTo(1L);
        assertThat(response.getOriginalFilename()).isEqualTo("test-image.jpg");
        assertThat(response.getImageOrder()).isEqualTo(1);
        assertThat(response.getFileSize()).isEqualTo(1024L);
        assertThat(response.getImageUrl()).contains("http://localhost:8080");
    }

    @Test
    @DisplayName("활성 이미지 개수를 정확히 반환한다")
    void getActiveImageCount() {
        // given - 이 테스트에서만 필요한 stubbing
        given(reviewImageRepository.countActiveImagesByReviewId(1L)).willReturn(3L);

        // when
        int result = reviewImageService.getActiveImageCount(1L);

        // then
        assertThat(result).isEqualTo(3);
        verify(reviewImageRepository, times(1)).countActiveImagesByReviewId(1L);
    }

    @Test
    @DisplayName("여러 이미지를 올바른 순서로 저장할 수 있다")
    void saveMultipleReviewImages() throws IOException {
        // given - 이 테스트에서만 필요한 모든 stubbing
        given(testReview.getReviewId()).willReturn(1L);

        MultipartFile firstImageFile = mock(MultipartFile.class);
        MultipartFile secondImageFile = mock(MultipartFile.class);
        List<MultipartFile> imageFiles = List.of(firstImageFile, secondImageFile);

        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo1 =
                ReviewImageUploadService.ReviewImageUploadInfo.builder()
                        .originalFilename("test-image1.jpg")
                        .storedFilename("uuid1.jpg")
                        .filePath("2024/01/01/uuid1.jpg")
                        .fileSize(1024L)
                        .contentType("image/jpeg")
                        .build();

        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo2 =
                ReviewImageUploadService.ReviewImageUploadInfo.builder()
                        .originalFilename("test-image2.jpg")
                        .storedFilename("uuid2.jpg")
                        .filePath("2024/01/01/uuid2.jpg")
                        .fileSize(2048L)
                        .contentType("image/jpeg")
                        .build();

        ReviewImage secondReviewImage = mock(ReviewImage.class);

        given(reviewImageRepository.countActiveImagesByReviewId(1L)).willReturn(0L);
        given(uploadService.uploadImage(firstImageFile)).willReturn(uploadInfo1);
        given(uploadService.uploadImage(secondImageFile)).willReturn(uploadInfo2);
        given(reviewImageRepository.save(any(ReviewImage.class)))
                .willReturn(testReviewImage)
                .willReturn(secondReviewImage);

        // when
        List<ReviewImage> result = reviewImageService.saveReviewImages(testReview, imageFiles);

        // then
        assertThat(result).hasSize(2);
        verify(uploadService, times(1)).validateImageCount(0, 2);
        verify(uploadService, times(2)).uploadImage(any());
        verify(reviewImageRepository, times(2)).save(any(ReviewImage.class));
    }

    // =================== 새로운 메서드 테스트 ===================

    @Test
    @DisplayName("선택된 이미지들을 성공적으로 삭제할 수 있다")
    void deleteSelectedImages_Success() {
        // given
        given(testReviewImage.getReviewImageId()).willReturn(1L);
        given(testReviewImage.getFilePath()).willReturn("2024/01/01/image1.jpg");

        ReviewImage secondImage = mock(ReviewImage.class);
        given(secondImage.getReviewImageId()).willReturn(2L);
        given(secondImage.getFilePath()).willReturn("2024/01/01/image2.jpg");

        List<Long> deleteImageIds = List.of(1L, 2L);
        List<ReviewImage> existingImages = List.of(testReviewImage, secondImage);

        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, deleteImageIds))
                .willReturn(existingImages);

        // when
        List<Long> deletedIds = reviewImageService.deleteSelectedImages(1L, deleteImageIds);

        // then
        assertThat(deletedIds).containsExactly(1L, 2L);

        // 선택된 이미지들만 삭제되었는지 확인
        verify(testReviewImage).delete();
        verify(secondImage).delete();

        // 파일 삭제도 호출되었는지 확인
        verify(uploadService).deleteImage("2024/01/01/image1.jpg");
        verify(uploadService).deleteImage("2024/01/01/image2.jpg");
    }

    @Test
    @DisplayName("빈 삭제 목록이 주어지면 아무것도 삭제하지 않는다")
    void deleteSelectedImages_EmptyList() {
        // given
        List<Long> emptyDeleteIds = List.of();

        // when
        List<Long> deletedIds = reviewImageService.deleteSelectedImages(1L, emptyDeleteIds);

        // then
        assertThat(deletedIds).isEmpty();
        verify(reviewImageRepository, never()).findActiveImagesByReviewIdAndImageIds(any(), any());
        verify(uploadService, never()).deleteImage(any());
    }

    @Test
    @DisplayName("null 삭제 목록이 주어지면 빈 리스트를 반환한다")
    void deleteSelectedImages_NullList() {
        // when
        List<Long> deletedIds = reviewImageService.deleteSelectedImages(1L, null);

        // then
        assertThat(deletedIds).isEmpty();
        verify(reviewImageRepository, never()).findActiveImagesByReviewIdAndImageIds(any(), any());
    }

    @Test
    @DisplayName("특정 이미지 하나만 삭제할 수 있다")
    void deleteSingleImage_Success() {
        // given
        given(testReviewImage.getReviewImageId()).willReturn(1L);
        given(testReviewImage.getFilePath()).willReturn("2024/01/01/image1.jpg");

        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, List.of(1L)))
                .willReturn(List.of(testReviewImage));

        // when
        boolean success = reviewImageService.deleteSingleImage(1L, 1L);

        // then
        assertThat(success).isTrue();
        verify(testReviewImage).delete();
        verify(uploadService).deleteImage("2024/01/01/image1.jpg");
    }

    @Test
    @DisplayName("존재하지 않는 이미지를 삭제하려 하면 false를 반환한다")
    void deleteSingleImage_NotFound() {
        // given - 존재하지 않는 이미지 ID로 조회하면 빈 리스트 반환
        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, List.of(999L)))
                .willReturn(List.of()); // 빈 리스트 반환

        // when
        boolean success = reviewImageService.deleteSingleImage(1L, 999L); // 999번 이미지 삭제 시도

        // then
        assertThat(success).isFalse();
        verify(uploadService, never()).deleteImage(any());
    }

    @Test
    @DisplayName("이미지 순서를 재정렬할 수 있다")
    void reorderImages_Success() {
        // given
        ReviewImage image2 = mock(ReviewImage.class);
        ReviewImage image3 = mock(ReviewImage.class);

        List<ReviewImage> activeImages = List.of(testReviewImage, image2, image3);
        given(reviewImageRepository.findActiveImagesByReviewId(1L))
                .willReturn(activeImages);

        // when
        reviewImageService.reorderImages(1L);

        // then
        verify(testReviewImage).updateOrder(1);
        verify(image2).updateOrder(2);
        verify(image3).updateOrder(3);
    }

    @Test
    @DisplayName("이미지가 없는 리뷰의 순서 재정렬 시 아무것도 하지 않는다")
    void reorderImages_EmptyImages() {
        // given
        given(reviewImageRepository.findActiveImagesByReviewId(1L))
                .willReturn(List.of());

        // when
        reviewImageService.reorderImages(1L);

        // then
        // 아무 이미지도 없으므로 updateOrder 호출되지 않음
        verify(testReviewImage, never()).updateOrder(any());
    }

    @Test
    @DisplayName("이미지 개수 제한을 확인할 수 있다")
    void canAddMoreImages() {
        // given
        given(reviewImageRepository.countActiveImagesByReviewId(1L)).willReturn(3L);
        given(imageProperties.getMaxImageCount()).willReturn(5);

        // when
        boolean canAdd2More = reviewImageService.canAddMoreImages(1L, 2); // 3 + 2 = 5 (허용)
        boolean cannotAdd3More = reviewImageService.canAddMoreImages(1L, 3); // 3 + 3 = 6 (초과)

        // then
        assertThat(canAdd2More).isTrue();
        assertThat(cannotAdd3More).isFalse();
    }

    // =================== 헬퍼 메서드 ===================

    private MultipartFile createTestImageFile(String filename) {
        MultipartFile imageFile = mock(MultipartFile.class);
        // 각 테스트에서 필요한 stubbing은 개별적으로 설정
        return imageFile;
    }
}