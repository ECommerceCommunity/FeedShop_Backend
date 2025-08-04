package com.cMall.feedShop.review.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("ReviewImage 엔티티 테스트")
class ReviewImageTest {

    private Review testReview;

    @BeforeEach
    void setUp() {
        testReview = mock(Review.class);
    }

    @Test
    @DisplayName("유효한 데이터로 ReviewImage를 생성할 수 있다")
    void createReviewImageWithValidData() {
        // when
        ReviewImage reviewImage = ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // then
        assertThat(reviewImage.getOriginalFilename()).isEqualTo("test-image.jpg");
        assertThat(reviewImage.getStoredFilename()).isEqualTo("uuid-filename.jpg");
        assertThat(reviewImage.getFilePath()).isEqualTo("2024/01/01/uuid-filename.jpg");
        assertThat(reviewImage.getFileSize()).isEqualTo(1024L);
        assertThat(reviewImage.getContentType()).isEqualTo("image/jpeg");
        assertThat(reviewImage.getImageOrder()).isEqualTo(1);
        assertThat(reviewImage.getReview()).isEqualTo(testReview);
        assertThat(reviewImage.getIsDeleted()).isFalse();
        assertThat(reviewImage.isActive()).isTrue();
    }

    @Test
    @DisplayName("이미지를 삭제할 수 있다")
    void deleteReviewImage() {
        // given
        ReviewImage reviewImage = ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // when
        reviewImage.delete();

        // then
        assertThat(reviewImage.getIsDeleted()).isTrue();
        assertThat(reviewImage.isActive()).isFalse();
    }

    @Test
    @DisplayName("완전한 이미지 URL을 생성할 수 있다")
    void getFullImageUrl() {
        // given
        ReviewImage reviewImage = ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();
        String baseUrl = "http://localhost:8080";

        // when
        String fullUrl = reviewImage.getFullImageUrl(baseUrl);

        // then
        assertThat(fullUrl).isEqualTo("http://localhost:8080/2024/01/01/uuid-filename.jpg");
    }

    @Test
    @DisplayName("원본 파일명이 null이면 예외가 발생한다")
    void createReviewImageWithNullOriginalFilename() {
        // when & then
        assertThatThrownBy(() -> ReviewImage.builder()
                .originalFilename(null)
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("원본 파일명은 필수입니다.");
    }

    @Test
    @DisplayName("원본 파일명이 빈 문자열이면 예외가 발생한다")
    void createReviewImageWithEmptyOriginalFilename() {
        // when & then
        assertThatThrownBy(() -> ReviewImage.builder()
                .originalFilename("   ")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("원본 파일명은 필수입니다.");
    }

    @Test
    @DisplayName("저장된 파일명이 null이면 예외가 발생한다")
    void createReviewImageWithNullStoredFilename() {
        // when & then
        assertThatThrownBy(() -> ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename(null)
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장된 파일명은 필수입니다.");
    }

    @Test
    @DisplayName("파일 경로가 null이면 예외가 발생한다")
    void createReviewImageWithNullFilePath() {
        // when & then
        assertThatThrownBy(() -> ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath(null)
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 경로는 필수입니다.");
    }

    @Test
    @DisplayName("파일 크기가 0 이하이면 예외가 발생한다")
    void createReviewImageWithInvalidFileSize() {
        // when & then
        assertThatThrownBy(() -> ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(0L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 크기는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("Content-Type이 이미지가 아니면 예외가 발생한다")
    void createReviewImageWithInvalidContentType() {
        // when & then
        assertThatThrownBy(() -> ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("text/plain")
                .imageOrder(1)
                .review(testReview)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지 파일만 업로드 가능합니다.");
    }

    @Test
    @DisplayName("Content-Type이 null이면 예외가 발생한다")
    void createReviewImageWithNullContentType() {
        // when & then
        assertThatThrownBy(() -> ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType(null)
                .imageOrder(1)
                .review(testReview)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지 파일만 업로드 가능합니다.");
    }
}