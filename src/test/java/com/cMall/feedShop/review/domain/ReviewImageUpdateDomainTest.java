package com.cMall.feedShop.review.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * 🔍 초보자 설명:
 * 이 테스트는 ReviewImage 도메인 객체의 수정 관련 메서드들이 올바르게 동작하는지 확인합니다.
 * - 이미지 순서 변경
 * - 이미지 정보 조회
 * - 이미지 복사 및 비교
 * - 파일 정보 처리
 */
@DisplayName("ReviewImage 도메인 수정 기능 테스트")
class ReviewImageUpdateDomainTest {

    private Review testReview;
    private ReviewImage testReviewImage;

    @BeforeEach
    void setUp() {
        testReview = mock(Review.class);

        testReviewImage = ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();
    }

    // =================== 이미지 순서 관리 테스트 ===================

    @Test
    @DisplayName("이미지 순서를 변경할 수 있다")
    void updateOrder_Success() {
        // when
        testReviewImage.updateOrder(3);

        // then
        assertThat(testReviewImage.getImageOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("이미지 순서를 1 미만으로 설정하면 예외가 발생한다")
    void updateOrder_InvalidOrder() {
        // when & then
        assertThatThrownBy(() -> testReviewImage.updateOrder(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지 순서는 1 이상이어야 합니다.");

        assertThatThrownBy(() -> testReviewImage.updateOrder(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지 순서는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("null 순서로 설정하면 예외가 발생한다")
    void updateOrder_NullOrder() {
        // when & then
        assertThatThrownBy(() -> testReviewImage.updateOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지 순서는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("이미지 순서를 한 칸씩 앞으로 이동할 수 있다")
    void moveUp() {
        // given
        testReviewImage.updateOrder(3);

        // when
        testReviewImage.moveUp();

        // then
        assertThat(testReviewImage.getImageOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("첫 번째 이미지는 앞으로 이동할 수 없다")
    void moveUp_FirstImage() {
        // given (초기 순서가 1)
        assertThat(testReviewImage.getImageOrder()).isEqualTo(1);

        // when
        testReviewImage.moveUp();

        // then
        assertThat(testReviewImage.getImageOrder()).isEqualTo(1); // 변경되지 않음
    }

    @Test
    @DisplayName("이미지 순서를 한 칸씩 뒤로 이동할 수 있다")
    void moveDown() {
        // when
        testReviewImage.moveDown();

        // then
        assertThat(testReviewImage.getImageOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("첫 번째 이미지인지 확인할 수 있다")
    void isFirst() {
        // when & then
        assertThat(testReviewImage.isFirst()).isTrue(); // 초기 순서가 1

        testReviewImage.updateOrder(2);
        assertThat(testReviewImage.isFirst()).isFalse();
    }

    @Test
    @DisplayName("두 이미지의 순서를 바꿀 수 있다")
    void swapOrderWith() {
        // given
        ReviewImage otherImage = ReviewImage.builder()
                .originalFilename("other-image.jpg")
                .storedFilename("other-uuid.jpg")
                .filePath("2024/01/01/other-uuid.jpg")
                .fileSize(2048L)
                .contentType("image/png")
                .imageOrder(3)
                .review(testReview)
                .build();

        // when
        testReviewImage.swapOrderWith(otherImage);

        // then
        assertThat(testReviewImage.getImageOrder()).isEqualTo(3);
        assertThat(otherImage.getImageOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("null 이미지와 순서를 바꾸려 하면 예외가 발생한다")
    void swapOrderWith_NullImage() {
        // when & then
        assertThatThrownBy(() -> testReviewImage.swapOrderWith(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("바꿀 이미지가 null입니다.");
    }

    // =================== 이미지 정보 조회 테스트 ===================

    @Test
    @DisplayName("파일 확장자를 추출할 수 있다")
    void getFileExtension() {
        // when & then
        assertThat(testReviewImage.getFileExtension()).isEqualTo("jpg");
    }

    @Test
    @DisplayName("확장자가 없는 파일명에서 빈 문자열을 반환한다")
    void getFileExtension_NoExtension() {
        // given
        ReviewImage imageWithoutExtension = ReviewImage.builder()
                .originalFilename("filename_without_extension")
                .storedFilename("uuid")
                .filePath("2024/01/01/uuid")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // when & then
        assertThat(imageWithoutExtension.getFileExtension()).isEmpty();
    }

    @Test
    @DisplayName("원본 파일명이 null인 경우 빈 문자열을 반환한다")
    void getFileExtension_NullFilename() {
        // given - ReflectionTestUtils로 필드 직접 수정
        ReviewImage imageWithNullFilename = ReviewImage.builder()
                .originalFilename("temp.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // originalFilename을 null로 직접 설정
        ReflectionTestUtils.setField(imageWithNullFilename, "originalFilename", null);

        // when & then
        assertThat(imageWithNullFilename.getFileExtension()).isEmpty();
    }

    @Test
    @DisplayName("파일 크기를 MB 단위로 변환할 수 있다")
    void getFileSizeInMB() {
        // given
        ReviewImage largeImage = ReviewImage.builder()
                .originalFilename("large-image.jpg")
                .storedFilename("uuid-large.jpg")
                .filePath("2024/01/01/uuid-large.jpg")
                .fileSize(2L * 1024L * 1024L) // 2MB
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // when & then
        assertThat(largeImage.getFileSizeInMB()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("파일 크기가 null인 경우 0.0을 반환한다")
    void getFileSizeInMB_NullSize() {
        // given - ReflectionTestUtils로 필드 직접 수정
        ReviewImage imageWithNullSize = ReviewImage.builder()
                .originalFilename("test.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // fileSize를 null로 직접 설정
        ReflectionTestUtils.setField(imageWithNullSize, "fileSize", null);

        // when & then
        assertThat(imageWithNullSize.getFileSizeInMB()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("파일 크기를 읽기 쉬운 형태로 변환할 수 있다")
    void getFormattedFileSize() {
        // when & then
        assertThat(testReviewImage.getFormattedFileSize()).isEqualTo("1.0 KB");

        // 다양한 크기 테스트
        ReviewImage smallImage = createImageWithSize(512L);
        assertThat(smallImage.getFormattedFileSize()).isEqualTo("512 B");

        ReviewImage mediumImage = createImageWithSize(1024L * 1024L);
        assertThat(mediumImage.getFormattedFileSize()).isEqualTo("1.0 MB");

        ReviewImage largeImage = createImageWithSize(2L * 1024L * 1024L + 512L * 1024L);
        assertThat(largeImage.getFormattedFileSize()).isEqualTo("2.5 MB");
    }

    @Test
    @DisplayName("파일 크기가 0인 경우 0 B를 반환한다")
    void getFormattedFileSize_ZeroSize() {
        // given - ReflectionTestUtils로 필드 직접 수정하여 검증 우회
        ReviewImage zeroSizeImage = ReviewImage.builder()
                .originalFilename("test.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1L) // 일단 유효한 값으로 생성
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // 생성 후 fileSize를 0으로 변경
        ReflectionTestUtils.setField(zeroSizeImage, "fileSize", 0L);

        // when & then
        assertThat(zeroSizeImage.getFormattedFileSize()).isEqualTo("0 B");
    }

    @Test
    @DisplayName("특정 이미지 형식인지 확인할 수 있다")
    void isImageType() {
        // when & then
        assertThat(testReviewImage.isImageType("jpeg")).isTrue();
        assertThat(testReviewImage.isImageType("jpg")).isFalse(); // content-type이 "image/jpeg"이므로 "jpg"는 포함되지 않음
        assertThat(testReviewImage.isImageType("png")).isFalse();
        assertThat(testReviewImage.isImageType("gif")).isFalse();
    }

    @Test
    @DisplayName("JPEG 이미지인지 확인할 수 있다")
    void isJpeg() {
        // when & then
        assertThat(testReviewImage.isJpeg()).isTrue();

        // PNG 이미지 테스트
        ReviewImage pngImage = ReviewImage.builder()
                .originalFilename("test.png")
                .storedFilename("uuid.png")
                .filePath("2024/01/01/uuid.png")
                .fileSize(1024L)
                .contentType("image/png")
                .imageOrder(1)
                .review(testReview)
                .build();

        assertThat(pngImage.isJpeg()).isFalse();
    }

    @Test
    @DisplayName("PNG 이미지인지 확인할 수 있다")
    void isPng() {
        // when & then
        assertThat(testReviewImage.isPng()).isFalse();

        // PNG 이미지 테스트
        ReviewImage pngImage = ReviewImage.builder()
                .originalFilename("test.png")
                .storedFilename("uuid.png")
                .filePath("2024/01/01/uuid.png")
                .fileSize(1024L)
                .contentType("image/png")
                .imageOrder(1)
                .review(testReview)
                .build();

        assertThat(pngImage.isPng()).isTrue();
    }

    @Test
    @DisplayName("WebP 이미지인지 확인할 수 있다")
    void isWebP() {
        // when & then
        assertThat(testReviewImage.isWebP()).isFalse();

        // WebP 이미지 테스트
        ReviewImage webpImage = ReviewImage.builder()
                .originalFilename("test.webp")
                .storedFilename("uuid.webp")
                .filePath("2024/01/01/uuid.webp")
                .fileSize(1024L)
                .contentType("image/webp")
                .imageOrder(1)
                .review(testReview)
                .build();

        assertThat(webpImage.isWebP()).isTrue();
    }

    // =================== 복사 및 비교 테스트 ===================

    @Test
    @DisplayName("새로운 리뷰용으로 이미지를 복사할 수 있다")
    void copyForNewReview() {
        // given
        Review newReview = mock(Review.class);

        // when
        ReviewImage copiedImage = testReviewImage.copyForNewReview(newReview, 2);

        // then
        assertThat(copiedImage.getOriginalFilename()).isEqualTo(testReviewImage.getOriginalFilename());
        assertThat(copiedImage.getStoredFilename()).isEqualTo(testReviewImage.getStoredFilename());
        assertThat(copiedImage.getFilePath()).isEqualTo(testReviewImage.getFilePath());
        assertThat(copiedImage.getFileSize()).isEqualTo(testReviewImage.getFileSize());
        assertThat(copiedImage.getContentType()).isEqualTo(testReviewImage.getContentType());
        assertThat(copiedImage.getImageOrder()).isEqualTo(2); // 새로운 순서
        assertThat(copiedImage.getReview()).isEqualTo(newReview); // 새로운 리뷰
    }

    @Test
    @DisplayName("같은 파일인지 확인할 수 있다")
    void isSameFile() {
        // given
        ReviewImage sameFileImage = ReviewImage.builder()
                .originalFilename("different-name.jpg")
                .storedFilename("different-uuid.jpg")
                .filePath("2024/01/01/uuid-filename.jpg") // 같은 파일 경로
                .fileSize(2048L)
                .contentType("image/png")
                .imageOrder(2)
                .review(testReview)
                .build();

        ReviewImage differentFileImage = ReviewImage.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/02/different-path.jpg") // 다른 파일 경로
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // when & then
        assertThat(testReviewImage.isSameFile(sameFileImage)).isTrue();
        assertThat(testReviewImage.isSameFile(differentFileImage)).isFalse();
        assertThat(testReviewImage.isSameFile(null)).isFalse();
    }

    @Test
    @DisplayName("같은 순서인지 확인할 수 있다")
    void hasSameOrder() {
        // given
        ReviewImage sameOrderImage = ReviewImage.builder()
                .originalFilename("other-image.jpg")
                .storedFilename("other-uuid.jpg")
                .filePath("2024/01/01/other-uuid.jpg")
                .fileSize(2048L)
                .contentType("image/png")
                .imageOrder(1) // 같은 순서
                .review(testReview)
                .build();

        ReviewImage differentOrderImage = ReviewImage.builder()
                .originalFilename("other-image.jpg")
                .storedFilename("other-uuid.jpg")
                .filePath("2024/01/01/other-uuid.jpg")
                .fileSize(2048L)
                .contentType("image/png")
                .imageOrder(2) // 다른 순서
                .review(testReview)
                .build();

        // when & then
        assertThat(testReviewImage.hasSameOrder(sameOrderImage)).isTrue();
        assertThat(testReviewImage.hasSameOrder(differentOrderImage)).isFalse();
        assertThat(testReviewImage.hasSameOrder(null)).isFalse();
    }

    // =================== 헬퍼 메서드 ===================

    private ReviewImage createImageWithSize(Long size) {
        return ReviewImage.builder()
                .originalFilename("test.jpg")
                .storedFilename("uuid.jpg")
                .filePath("2024/01/01/uuid.jpg")
                .fileSize(size)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();
    }

    // =================== 추가 엣지 케이스 테스트 ===================

    @Test
    @DisplayName("Content-Type이 null인 경우 이미지 형식 확인에서 false를 반환한다")
    void isImageType_NullContentType() {
        // given - ReflectionTestUtils로 필드 직접 수정
        ReviewImage imageWithNullContentType = ReviewImage.builder()
                .originalFilename("test.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // contentType을 null로 직접 설정
        ReflectionTestUtils.setField(imageWithNullContentType, "contentType", null);

        // when & then
        assertThat(imageWithNullContentType.isImageType("jpeg")).isFalse();
        assertThat(imageWithNullContentType.isJpeg()).isFalse();
        assertThat(imageWithNullContentType.isPng()).isFalse();
        assertThat(imageWithNullContentType.isWebP()).isFalse();
    }

    @Test
    @DisplayName("파일 경로가 null인 경우 같은 파일 비교에서 false를 반환한다")
    void isSameFile_NullFilePath() {
        // given - ReflectionTestUtils로 필드 직접 수정
        ReviewImage imageWithNullPath = ReviewImage.builder()
                .originalFilename("test.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // filePath를 null로 직접 설정
        ReflectionTestUtils.setField(imageWithNullPath, "filePath", null);

        ReviewImage otherImage = ReviewImage.builder()
                .originalFilename("other.jpg")
                .storedFilename("other-uuid.jpg")
                .filePath("some/path.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // when & then
        assertThat(imageWithNullPath.isSameFile(otherImage)).isFalse();
    }

    @Test
    @DisplayName("이미지 순서가 null인 경우 같은 순서 비교에서 false를 반환한다")
    void hasSameOrder_NullOrder() {
        // given - ReflectionTestUtils로 필드 직접 수정
        ReviewImage imageWithNullOrder = ReviewImage.builder()
                .originalFilename("test.jpg")
                .storedFilename("uuid-filename.jpg")
                .filePath("2024/01/01/uuid-filename.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // imageOrder를 null로 직접 설정
        ReflectionTestUtils.setField(imageWithNullOrder, "imageOrder", null);

        ReviewImage otherImage = ReviewImage.builder()
                .originalFilename("other.jpg")
                .storedFilename("other-uuid.jpg")
                .filePath("2024/01/01/other-uuid.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        // when & then
        assertThat(imageWithNullOrder.hasSameOrder(otherImage)).isFalse();
    }
}