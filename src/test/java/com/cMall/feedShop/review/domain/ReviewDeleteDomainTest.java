package com.cMall.feedShop.review.domain;

import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("Review 도메인 삭제 기능 테스트")
class ReviewDeleteDomainTest {

    private User testUser;
    private User otherUser;
    private Product testProduct;
    private Review testReview;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 설정 (기존 방식 사용)
        testUser = new User("testLogin", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 다른 사용자 설정
        otherUser = new User("otherLogin", "password456", "other@example.com", UserRole.USER);
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        // 테스트 상품 설정 (기존 방식 사용)
        Store testStore = mock(Store.class);
        Category testCategory = mock(Category.class);

        testProduct = Product.builder()
                .name("테스트 신발")
                .description("테스트 신발 설명")
                .price(new BigDecimal("100000"))
                .category(testCategory)
                .store(testStore)
                .discountType(DiscountType.NONE)
                .discountValue(null)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        // 테스트 리뷰 설정
        testReview = Review.builder()
                .title("테스트 리뷰")
                .content("테스트 리뷰 내용입니다.")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(testReview, "reviewId", 1L);
    }

    @Test
    @DisplayName("markAsDeleted - 리뷰를 삭제 상태로 변경")
    void markAsDeleted_Success() {
        // Given
        assertThat(testReview.getStatus()).isEqualTo(ReviewStatus.ACTIVE);

        // When
        testReview.markAsDeleted();

        // Then
        assertThat(testReview.getStatus()).isEqualTo(ReviewStatus.DELETED);
    }

    @Test
    @DisplayName("markAsDeleted - 여러 번 호출해도 상태 유지")
    void markAsDeleted_MultipleCallsSuccess() {
        // Given
        assertThat(testReview.getStatus()).isEqualTo(ReviewStatus.ACTIVE);

        // When
        testReview.markAsDeleted();
        testReview.markAsDeleted(); // 두 번 호출

        // Then
        assertThat(testReview.getStatus()).isEqualTo(ReviewStatus.DELETED);
    }

    @Test
    @DisplayName("isActive - 삭제된 리뷰는 비활성 상태")
    void isActive_DeletedReviewReturnsFalse() {
        // Given
        testReview.markAsDeleted();

        // When
        boolean isActive = testReview.isActive();

        // Then
        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("isActive - 활성 리뷰는 활성 상태")
    void isActive_ActiveReviewReturnsTrue() {
        // Given - 초기 상태는 ACTIVE

        // When
        boolean isActive = testReview.isActive();

        // Then
        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("canBeUpdatedBy - 삭제된 리뷰는 수정 불가")
    void canBeUpdatedBy_DeletedReviewReturnsFalse() {
        // Given
        testReview.markAsDeleted();

        // When
        boolean canUpdate = testReview.canBeUpdatedBy(testUser.getId());

        // Then
        assertThat(canUpdate).isFalse();
    }

    @Test
    @DisplayName("canBeUpdatedBy - 활성 리뷰는 소유자가 수정 가능")
    void canBeUpdatedBy_ActiveReviewOwnerReturnsTrue() {
        // Given - 초기 상태는 ACTIVE

        // When
        boolean canUpdate = testReview.canBeUpdatedBy(testUser.getId());

        // Then
        assertThat(canUpdate).isTrue();
    }

    @Test
    @DisplayName("canBeUpdatedBy - 다른 사용자는 수정 불가")
    void canBeUpdatedBy_OtherUserReturnsFalse() {
        // Given - 초기 상태는 ACTIVE

        // When
        boolean canUpdate = testReview.canBeUpdatedBy(otherUser.getId());

        // Then
        assertThat(canUpdate).isFalse();
    }

    @Test
    @DisplayName("validateUpdatePermission - 삭제된 리뷰 수정 시 예외 발생")
    void validateUpdatePermission_DeletedReviewThrowsException() {
        // Given
        testReview.markAsDeleted();

        // When & Then
        assertThatThrownBy(() -> testReview.validateUpdatePermission(testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제되거나 숨김 처리된 리뷰는 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("validateUpdatePermission - 다른 사용자 수정 시 예외 발생")
    void validateUpdatePermission_OtherUserThrowsException() {
        // Given - 초기 상태는 ACTIVE

        // When & Then
        assertThatThrownBy(() -> testReview.validateUpdatePermission(otherUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 작성한 리뷰만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("validateUpdatePermission - 소유자는 수정 권한 있음")
    void validateUpdatePermission_OwnerSuccess() {
        // Given - 초기 상태는 ACTIVE

        // When & Then (예외가 발생하지 않아야 함)
        testReview.validateUpdatePermission(testUser.getId());
    }

    @Test
    @DisplayName("isUpdatable - 삭제된 리뷰는 수정 불가")
    void isUpdatable_DeletedReviewReturnsFalse() {
        // Given
        testReview.markAsDeleted();

        // When
        boolean isUpdatable = testReview.isUpdatable();

        // Then
        assertThat(isUpdatable).isFalse();
    }

    @Test
    @DisplayName("isUpdatable - 활성 리뷰는 수정 가능")
    void isUpdatable_ActiveReviewReturnsTrue() {
        // Given - 초기 상태는 ACTIVE

        // When
        boolean isUpdatable = testReview.isUpdatable();

        // Then
        assertThat(isUpdatable).isTrue();
    }

    @Test
    @DisplayName("removeImagesByIds - 이미지 ID 목록으로 삭제")
    void removeImagesByIds_Success() {
        // Given
        ReviewImage image1 = ReviewImage.builder()
                .originalFilename("image1.jpg")
                .storedFilename("stored-image1.jpg")
                .filePath("2024/01/01/stored-image1.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();
        ReflectionTestUtils.setField(image1, "reviewImageId", 1L);

        ReviewImage image2 = ReviewImage.builder()
                .originalFilename("image2.jpg")
                .storedFilename("stored-image2.jpg")
                .filePath("2024/01/01/stored-image2.jpg")
                .fileSize(2048L)
                .contentType("image/jpeg")
                .imageOrder(2)
                .review(testReview)
                .build();
        ReflectionTestUtils.setField(image2, "reviewImageId", 2L);

        ReviewImage image3 = ReviewImage.builder()
                .originalFilename("image3.jpg")
                .storedFilename("stored-image3.jpg")
                .filePath("2024/01/01/stored-image3.jpg")
                .fileSize(3072L)
                .contentType("image/jpeg")
                .imageOrder(3)
                .review(testReview)
                .build();
        ReflectionTestUtils.setField(image3, "reviewImageId", 3L);

        // 리뷰에 이미지들 추가
        ReflectionTestUtils.setField(testReview, "images", new ArrayList<>(Arrays.asList(image1, image2, image3)));

        // When
        testReview.removeImagesByIds(Arrays.asList(1L, 3L));

        // Then
        assertThat(testReview.getImages()).hasSize(1); // image2만 남아야 함
        assertThat(testReview.getImages().get(0)).isEqualTo(image2);
    }

    @Test
    @DisplayName("removeImagesByIds - 빈 목록 전달 시 아무 변화 없음")
    void removeImagesByIds_EmptyListNoChange() {
        // Given
        ReviewImage image1 = ReviewImage.builder()
                .originalFilename("image1.jpg")
                .storedFilename("stored-image1.jpg")
                .filePath("2024/01/01/stored-image1.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();
        ReflectionTestUtils.setField(image1, "reviewImageId", 1L);

        ReflectionTestUtils.setField(testReview, "images", new ArrayList<>(Arrays.asList(image1)));
        int originalSize = testReview.getImages().size();

        // When
        testReview.removeImagesByIds(Arrays.asList());

        // Then
        assertThat(testReview.getImages()).hasSize(originalSize);
    }

    @Test
    @DisplayName("removeImagesByIds - null 전달 시 아무 변화 없음")
    void removeImagesByIds_NullListNoChange() {
        // Given
        ReviewImage image1 = ReviewImage.builder()
                .originalFilename("image1.jpg")
                .storedFilename("stored-image1.jpg")
                .filePath("2024/01/01/stored-image1.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .review(testReview)
                .build();

        ReflectionTestUtils.setField(testReview, "images", new ArrayList<>(Arrays.asList(image1)));
        int originalSize = testReview.getImages().size();

        // When
        testReview.removeImagesByIds(null);

        // Then
        assertThat(testReview.getImages()).hasSize(originalSize);
    }
}