package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReviewImageRepositoryImpl 테스트")
class ReviewImageRepositoryImplTest {

    @Mock
    private ReviewImageJpaRepository reviewImageJpaRepository;

    @InjectMocks
    private ReviewImageRepositoryImpl reviewImageRepository;

    private ReviewImage testReviewImage;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testReviewImage = mock(ReviewImage.class);
        testReview = mock(Review.class);
        given(testReviewImage.getReviewImageId()).willReturn(1L);
        given(testReview.getReviewId()).willReturn(1L);
    }

    @Test
    @DisplayName("리뷰 이미지를 성공적으로 저장할 수 있다")
    void saveReviewImage() {
        // given
        given(reviewImageJpaRepository.save(any(ReviewImage.class))).willReturn(testReviewImage);

        // when
        ReviewImage savedImage = reviewImageRepository.save(testReviewImage);

        // then
        assertThat(savedImage).isEqualTo(testReviewImage);
        verify(reviewImageJpaRepository, times(1)).save(testReviewImage);
    }

    @Test
    @DisplayName("리뷰별 활성 이미지 목록을 조회할 수 있다")
    void findActiveImagesByReviewId() {
        // given
        List<ReviewImage> images = List.of(testReviewImage);
        given(reviewImageJpaRepository.findActiveImagesByReviewId(1L)).willReturn(images);

        // when
        List<ReviewImage> result = reviewImageRepository.findActiveImagesByReviewId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testReviewImage);
        verify(reviewImageJpaRepository, times(1)).findActiveImagesByReviewId(1L);
    }

    @Test
    @DisplayName("Review 객체로 활성 이미지들을 순서대로 조회할 수 있다")
    void findActiveImagesByReview() {
        // given
        List<ReviewImage> images = List.of(testReviewImage);
        given(reviewImageJpaRepository.findActiveImagesByReview(testReview)).willReturn(images);

        // when
        List<ReviewImage> result = reviewImageRepository.findActiveImagesByReview(testReview);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testReviewImage);
        verify(reviewImageJpaRepository, times(1)).findActiveImagesByReview(testReview);
    }

    @Test
    @DisplayName("Review 객체로 활성 이미지들을 조회할 수 있다 (순서 무관)")
    void findByReviewAndIsDeletedFalse() {
        // given
        List<ReviewImage> images = List.of(testReviewImage);
        given(reviewImageJpaRepository.findByReviewAndIsDeletedFalse(testReview)).willReturn(images);

        // when
        List<ReviewImage> result = reviewImageRepository.findByReviewAndIsDeletedFalse(testReview);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testReviewImage);
        verify(reviewImageJpaRepository, times(1)).findByReviewAndIsDeletedFalse(testReview);
    }

    @Test
    @DisplayName("리뷰별 활성 이미지 개수를 조회할 수 있다")
    void countActiveImagesByReviewId() {
        // given - Long 타입으로 수정
        given(reviewImageJpaRepository.countActiveImagesByReviewId(1L)).willReturn(3L);

        // when - Long 타입으로 수정
        Long result = reviewImageRepository.countActiveImagesByReviewId(1L);

        // then
        assertThat(result).isEqualTo(3L);
        verify(reviewImageJpaRepository, times(1)).countActiveImagesByReviewId(1L);
    }

    @Test
    @DisplayName("여러 리뷰의 활성 이미지들을 한 번에 조회할 수 있다")
    void findActiveImagesByReviewIds() {
        // given
        List<Long> reviewIds = List.of(1L, 2L, 3L);
        List<ReviewImage> images = List.of(testReviewImage);
        given(reviewImageJpaRepository.findActiveImagesByReviewIds(reviewIds)).willReturn(images);

        // when
        List<ReviewImage> result = reviewImageRepository.findActiveImagesByReviewIds(reviewIds);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testReviewImage);
        verify(reviewImageJpaRepository, times(1)).findActiveImagesByReviewIds(reviewIds);
    }

    @Test
    @DisplayName("여러 리뷰 이미지를 한 번에 저장할 수 있다")
    void saveAllReviewImages() {
        // given
        ReviewImage secondImage = mock(ReviewImage.class);
        List<ReviewImage> images = List.of(testReviewImage, secondImage);
        given(reviewImageJpaRepository.saveAll(images)).willReturn(images);

        // when
        List<ReviewImage> result = reviewImageRepository.saveAll(images);

        // then
        assertThat(result).hasSize(2);
        verify(reviewImageJpaRepository, times(1)).saveAll(images);
    }

    @Test
    @DisplayName("리뷰 이미지를 삭제할 수 있다")
    void deleteReviewImage() {
        // given
        doNothing().when(reviewImageJpaRepository).delete(testReviewImage);

        // when
        reviewImageRepository.delete(testReviewImage);

        // then
        verify(reviewImageJpaRepository, times(1)).delete(testReviewImage);
    }

    @Test
    @DisplayName("여러 리뷰 이미지를 한 번에 삭제할 수 있다")
    void deleteAllReviewImages() {
        // given
        ReviewImage secondImage = mock(ReviewImage.class);
        List<ReviewImage> images = List.of(testReviewImage, secondImage);
        doNothing().when(reviewImageJpaRepository).deleteAll(images);

        // when
        reviewImageRepository.deleteAll(images);

        // then
        verify(reviewImageJpaRepository, times(1)).deleteAll(images);
    }

    @Test
    @DisplayName("활성 이미지가 없는 리뷰의 경우 빈 리스트를 반환한다")
    void findActiveImagesByReviewId_EmptyResult() {
        // given
        given(reviewImageJpaRepository.findActiveImagesByReviewId(999L)).willReturn(List.of());

        // when
        List<ReviewImage> result = reviewImageRepository.findActiveImagesByReviewId(999L);

        // then
        assertThat(result).isEmpty();
        verify(reviewImageJpaRepository, times(1)).findActiveImagesByReviewId(999L);
    }

    @Test
    @DisplayName("활성 이미지 개수가 0인 경우 0을 반환한다")
    void countActiveImagesByReviewId_ZeroResult() {
        // given - Long 타입으로 수정
        given(reviewImageJpaRepository.countActiveImagesByReviewId(999L)).willReturn(0L);

        // when - Long 타입으로 수정
        Long result = reviewImageRepository.countActiveImagesByReviewId(999L);

        // then
        assertThat(result).isEqualTo(0L);
        verify(reviewImageJpaRepository, times(1)).countActiveImagesByReviewId(999L);
    }

    @Test
    @DisplayName("Review 객체로 조회할 때 이미지가 없으면 빈 리스트를 반환한다")
    void findActiveImagesByReview_EmptyResult() {
        // given
        given(reviewImageJpaRepository.findActiveImagesByReview(testReview)).willReturn(List.of());

        // when
        List<ReviewImage> result = reviewImageRepository.findActiveImagesByReview(testReview);

        // then
        assertThat(result).isEmpty();
        verify(reviewImageJpaRepository, times(1)).findActiveImagesByReview(testReview);
    }

    @Test
    @DisplayName("여러 리뷰 ID로 조회할 때 이미지가 없으면 빈 리스트를 반환한다")
    void findActiveImagesByReviewIds_EmptyResult() {
        // given
        List<Long> reviewIds = List.of(999L, 998L);
        given(reviewImageJpaRepository.findActiveImagesByReviewIds(reviewIds)).willReturn(List.of());

        // when
        List<ReviewImage> result = reviewImageRepository.findActiveImagesByReviewIds(reviewIds);

        // then
        assertThat(result).isEmpty();
        verify(reviewImageJpaRepository, times(1)).findActiveImagesByReviewIds(reviewIds);
    }
}