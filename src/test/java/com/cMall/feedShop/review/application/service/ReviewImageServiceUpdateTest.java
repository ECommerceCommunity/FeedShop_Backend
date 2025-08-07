package com.cMall.feedShop.review.application.service;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 🔍 초보자 설명:
 * 이 테스트는 리뷰 이미지 수정 기능이 올바르게 동작하는지 확인합니다.
 * - 기존 이미지 삭제
 * - 새로운 이미지 추가
 * - 이미지 순서 재정렬
 * - 개수 제한 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewImageService 수정 기능 테스트")
class ReviewImageServiceUpdateTest {

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private ReviewImageUploadService uploadService;

    @Mock
    private ReviewImageProperties imageProperties;

    @InjectMocks
    private ReviewImageService reviewImageService;

    private Review testReview;
    private ReviewImage testImage1;
    private ReviewImage testImage2;
    private ReviewImage testImage3;

    @BeforeEach
    void setUp() {
        // Mock 객체들만 생성, stubbing은 각 테스트에서 필요시 설정
        testReview = mock(Review.class);
        testImage1 = mock(ReviewImage.class);
        testImage2 = mock(ReviewImage.class);
        testImage3 = mock(ReviewImage.class);
    }

    @Test
    @DisplayName("선택된 이미지들을 성공적으로 삭제할 수 있다")
    void deleteSelectedImages_Success() {
        // given
        given(testImage1.getReviewImageId()).willReturn(1L);
        given(testImage1.getFilePath()).willReturn("2024/01/01/image1.jpg");
        given(testImage2.getReviewImageId()).willReturn(2L);
        given(testImage2.getFilePath()).willReturn("2024/01/01/image2.jpg");

        List<Long> deleteImageIds = List.of(1L, 2L);
        List<ReviewImage> existingImages = List.of(testImage1, testImage2);

        // ✨ 수정: 새로운 메서드 사용
        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, deleteImageIds))
                .willReturn(existingImages);

        // when
        List<Long> deletedIds = reviewImageService.deleteSelectedImages(1L, deleteImageIds);

        // then
        assertThat(deletedIds).containsExactly(1L, 2L);

        // 선택된 이미지들만 삭제되었는지 확인
        verify(testImage1).delete();
        verify(testImage2).delete();

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
    @DisplayName("존재하지 않는 이미지 ID로 삭제를 시도해도 오류가 발생하지 않는다")
    void deleteSelectedImages_NonExistentIds() {
        // given
        List<Long> deleteImageIds = List.of(999L, 998L);

        // ✨ 수정: 존재하지 않는 이미지 ID로 조회하면 빈 리스트 반환
        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, deleteImageIds))
                .willReturn(List.of());

        // when
        List<Long> deletedIds = reviewImageService.deleteSelectedImages(1L, deleteImageIds);

        // then
        assertThat(deletedIds).isEmpty(); // 존재하지 않는 ID이므로 삭제된 것 없음
        verify(uploadService, never()).deleteImage(any());
    }

    @Test
    @DisplayName("리뷰 이미지를 업데이트할 수 있다 (삭제 + 추가)")
    void updateReviewImages_Success() {
        // given
        given(testReview.getReviewId()).willReturn(1L);
        given(testImage1.getReviewImageId()).willReturn(1L);
        given(testImage1.getFilePath()).willReturn("2024/01/01/image1.jpg");

        List<Long> deleteImageIds = List.of(1L);
        MultipartFile newImageFile = mock(MultipartFile.class);
        List<MultipartFile> newImageFiles = List.of(newImageFile);

        // 삭제 관련 모킹 - 새로운 메서드 사용
        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, deleteImageIds))
                .willReturn(List.of(testImage1));

        // 삭제 후 남은 이미지 개수 모킹 (countActiveImagesByReviewId 호출 순서대로)
        given(reviewImageRepository.countActiveImagesByReviewId(1L))
                .willReturn(0L)  // updateReviewImages에서 getActiveImageCount 호출
                .willReturn(0L); // saveReviewImages에서 countActiveImagesByReviewId 호출

        // 새 이미지 추가 관련 모킹
        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo =
                ReviewImageUploadService.ReviewImageUploadInfo.builder()
                        .originalFilename("new-image.jpg")
                        .storedFilename("uuid-new-image.jpg")
                        .filePath("2024/01/02/uuid-new-image.jpg")
                        .fileSize(1024L)
                        .contentType("image/jpeg")
                        .build();

        given(uploadService.uploadImage(newImageFile)).willReturn(uploadInfo);
        given(reviewImageRepository.save(any(ReviewImage.class))).willReturn(mock(ReviewImage.class));

        // when
        List<ReviewImage> newImages = reviewImageService.updateReviewImages(
                testReview, deleteImageIds, newImageFiles);

        // then
        assertThat(newImages).hasSize(1);

        // 삭제 및 업로드 검증이 호출되었는지 확인
        verify(testImage1).delete();
        // validateImageCount는 두 번 호출됨 (updateReviewImages와 saveReviewImages에서)
        verify(uploadService, times(2)).validateImageCount(0, 1);
        verify(uploadService).uploadImage(newImageFile);
        verify(reviewImageRepository).save(any(ReviewImage.class));
    }

    @Test
    @DisplayName("특정 이미지 하나만 삭제할 수 있다")
    void deleteSingleImage_Success() {
        // given
        given(testImage1.getReviewImageId()).willReturn(1L);
        given(testImage1.getFilePath()).willReturn("2024/01/01/image1.jpg");

        // ✨ 수정: 새로운 메서드 사용 - 단일 이미지 ID 리스트로 조회
        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, List.of(1L)))
                .willReturn(List.of(testImage1));

        // when
        boolean success = reviewImageService.deleteSingleImage(1L, 1L);

        // then
        assertThat(success).isTrue();
        verify(testImage1).delete();
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
        List<ReviewImage> activeImages = List.of(testImage1, testImage2, testImage3);
        given(reviewImageRepository.findActiveImagesByReviewId(1L))
                .willReturn(activeImages);

        // when
        reviewImageService.reorderImages(1L);

        // then
        verify(testImage1).updateOrder(1);
        verify(testImage2).updateOrder(2);
        verify(testImage3).updateOrder(3);
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
        verify(testImage1, never()).updateOrder(any());
        verify(testImage2, never()).updateOrder(any());
        verify(testImage3, never()).updateOrder(any());
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

    @Test
    @DisplayName("파일 삭제 실패 시에도 논리적 삭제는 완료된다")
    void deleteSelectedImages_FileDeleteFailure() {
        // given
        given(testImage1.getReviewImageId()).willReturn(1L);
        given(testImage1.getFilePath()).willReturn("2024/01/01/image1.jpg");

        List<Long> deleteImageIds = List.of(1L);

        // ✨ 수정: 새로운 메서드 사용
        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, deleteImageIds))
                .willReturn(List.of(testImage1));

        // 파일 삭제 실패 시뮬레이션
        doThrow(new RuntimeException("파일 삭제 실패"))
                .when(uploadService).deleteImage("2024/01/01/image1.jpg");

        // when
        List<Long> deletedIds = reviewImageService.deleteSelectedImages(1L, deleteImageIds);

        // then
        // 실제 코드에서는 예외를 catch하므로 빈 리스트가 반환됨
        assertThat(deletedIds).isEmpty();
        verify(testImage1).delete(); // 논리적 삭제는 수행됨
        verify(uploadService).deleteImage("2024/01/01/image1.jpg");
    }

    @Test
    @DisplayName("이미지 업데이트 시 개수 제한을 검증한다")
    void updateReviewImages_ValidateImageCount() {
        // given
        given(testReview.getReviewId()).willReturn(1L);
        List<MultipartFile> tooManyImages = List.of(
                mock(MultipartFile.class),
                mock(MultipartFile.class),
                mock(MultipartFile.class)
        );

        given(reviewImageRepository.countActiveImagesByReviewId(1L)).willReturn(3L);

        // 개수 제한 검증에서 예외 발생하도록 설정
        doThrow(new RuntimeException("이미지는 최대 5개까지만 업로드할 수 있습니다."))
                .when(uploadService).validateImageCount(3, 3);

        // when & then
        assertThatThrownBy(() ->
                reviewImageService.updateReviewImages(testReview, List.of(), tooManyImages))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미지는 최대 5개까지만 업로드할 수 있습니다");

        verify(uploadService).validateImageCount(3, 3);
    }

    @Test
    @DisplayName("이미지 삭제와 추가가 동시에 일어날 때 순서대로 처리된다")
    void updateReviewImages_DeleteAndAdd() {
        // given
        given(testReview.getReviewId()).willReturn(1L);
        given(testImage1.getReviewImageId()).willReturn(1L);
        given(testImage1.getFilePath()).willReturn("2024/01/01/image1.jpg");
        given(testImage2.getReviewImageId()).willReturn(2L);
        given(testImage2.getFilePath()).willReturn("2024/01/01/image2.jpg");

        List<Long> deleteImageIds = List.of(1L, 2L); // 2개 삭제
        MultipartFile newImage = mock(MultipartFile.class);
        List<MultipartFile> newImageFiles = List.of(newImage); // 1개 추가

        // ✨ 수정: 새로운 메서드 사용 - 삭제할 이미지들만 조회
        given(reviewImageRepository.findActiveImagesByReviewIdAndImageIds(1L, deleteImageIds))
                .willReturn(List.of(testImage1, testImage2));

        // 삭제 후 1개 이미지 남음
        given(reviewImageRepository.countActiveImagesByReviewId(1L))
                .willReturn(1L)  // updateReviewImages에서 getActiveImageCount 호출
                .willReturn(1L); // saveReviewImages에서 countActiveImagesByReviewId 호출

        // 새 이미지 업로드 모킹
        ReviewImageUploadService.ReviewImageUploadInfo uploadInfo =
                ReviewImageUploadService.ReviewImageUploadInfo.builder()
                        .originalFilename("new-image.jpg")
                        .storedFilename("uuid-new.jpg")
                        .filePath("2024/01/02/uuid-new.jpg")
                        .fileSize(2048L)
                        .contentType("image/jpeg")
                        .build();

        given(uploadService.uploadImage(newImage)).willReturn(uploadInfo);
        given(reviewImageRepository.save(any(ReviewImage.class))).willReturn(testImage3);

        // when
        List<ReviewImage> result = reviewImageService.updateReviewImages(
                testReview, deleteImageIds, newImageFiles);

        // then
        assertThat(result).hasSize(1);

        // 삭제가 먼저 수행되었는지 확인
        verify(testImage1).delete();
        verify(testImage2).delete();

        // 그 다음 추가가 수행되었는지 확인 - validateImageCount는 두 번 호출됨
        verify(uploadService, times(2)).validateImageCount(1, 1);
        verify(uploadService).uploadImage(newImage);
        verify(reviewImageRepository).save(any(ReviewImage.class));
    }
}