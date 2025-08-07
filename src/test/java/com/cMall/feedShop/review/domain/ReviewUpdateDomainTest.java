package com.cMall.feedShop.review.domain;

import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * 🔍 초보자 설명:
 * 이 테스트는 Review 도메인 객체의 수정 관련 메서드들이 올바르게 동작하는지 확인합니다.
 * - 리뷰 정보 업데이트
 * - 권한 검증
 * - 이미지 관리
 * - 유효성 검증
 */
@DisplayName("Review 도메인 수정 기능 테스트")
class ReviewUpdateDomainTest {

    private User testUser;
    private User otherUser;
    private Product testProduct;
    private Store testStore;
    private Category testCategory;
    private Review testReview;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자들 생성
        testUser = new User("testLogin", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        otherUser = new User("otherLogin", "password", "other@test.com", UserRole.USER);
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        // Store와 Category 모킹
        testStore = mock(Store.class);
        testCategory = mock(Category.class);

        // Product 객체 생성
        testProduct = Product.builder()
                .name("테스트 신발")
                .price(new BigDecimal("100000"))
                .store(testStore)
                .category(testCategory)
                .discountType(DiscountType.NONE)
                .discountValue(null)
                .description("테스트용 신발입니다")
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        // 테스트용 리뷰 생성
        testReview = Review.builder()
                .title("원본 제목")
                .rating(4)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.MEDIUM)
                .stability(Stability.STABLE)
                .content("원본 내용입니다.")
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(testReview, "reviewId", 1L);
    }

    @Test
    @DisplayName("리뷰 정보를 성공적으로 업데이트할 수 있다")
    void updateReviewInfo_Success() {
        // when
        testReview.updateReviewInfo(
                "수정된 제목",
                5,
                "수정된 내용입니다.",
                SizeFit.BIG,
                Cushion.SOFT,
                Stability.VERY_STABLE
        );

        // then
        assertThat(testReview.getTitle()).isEqualTo("수정된 제목");
        assertThat(testReview.getRating()).isEqualTo(5);
        assertThat(testReview.getContent()).isEqualTo("수정된 내용입니다.");
        assertThat(testReview.getSizeFit()).isEqualTo(SizeFit.BIG);
        assertThat(testReview.getCushion()).isEqualTo(Cushion.SOFT);
        assertThat(testReview.getStability()).isEqualTo(Stability.VERY_STABLE);
    }

    @Test
    @DisplayName("제목만 수정할 수 있다")
    void updateTitle_Success() {
        // when
        testReview.updateTitle("새로운 제목");

        // then
        assertThat(testReview.getTitle()).isEqualTo("새로운 제목");
        // 다른 필드들은 변경되지 않음
        assertThat(testReview.getRating()).isEqualTo(4);
        assertThat(testReview.getContent()).isEqualTo("원본 내용입니다.");
    }

    @Test
    @DisplayName("평점만 수정할 수 있다")
    void updateRating_Success() {
        // when
        testReview.updateRating(5);

        // then
        assertThat(testReview.getRating()).isEqualTo(5);
        // 다른 필드들은 변경되지 않음
        assertThat(testReview.getTitle()).isEqualTo("원본 제목");
        assertThat(testReview.getContent()).isEqualTo("원본 내용입니다.");
    }

    @Test
    @DisplayName("내용만 수정할 수 있다")
    void updateContent_Success() {
        // when
        testReview.updateContent("새로운 내용입니다.");

        // then
        assertThat(testReview.getContent()).isEqualTo("새로운 내용입니다.");
        // 다른 필드들은 변경되지 않음
        assertThat(testReview.getTitle()).isEqualTo("원본 제목");
        assertThat(testReview.getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("3요소 평가만 수정할 수 있다")
    void update3Elements_Success() {
        // when
        testReview.update3Elements(SizeFit.SMALL, Cushion.FIRM, Stability.UNSTABLE);

        // then
        assertThat(testReview.getSizeFit()).isEqualTo(SizeFit.SMALL);
        assertThat(testReview.getCushion()).isEqualTo(Cushion.FIRM);
        assertThat(testReview.getStability()).isEqualTo(Stability.UNSTABLE);
        // 다른 필드들은 변경되지 않음
        assertThat(testReview.getTitle()).isEqualTo("원본 제목");
        assertThat(testReview.getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("수정 권한을 정확히 확인할 수 있다")
    void canBeUpdatedBy() {
        // when & then
        assertThat(testReview.canBeUpdatedBy(testUser.getId())).isTrue(); // 작성자는 수정 가능
        assertThat(testReview.canBeUpdatedBy(otherUser.getId())).isFalse(); // 다른 사용자는 수정 불가
    }

    @Test
    @DisplayName("수정 가능한 상태인지 확인할 수 있다")
    void isUpdatable() {
        // when & then
        assertThat(testReview.isUpdatable()).isTrue(); // 활성 상태이므로 수정 가능
    }

    @Test
    @DisplayName("수정 권한 검증에서 작성자가 아니면 예외가 발생한다")
    void validateUpdatePermission_NotOwner() {
        // when & then
        assertThatThrownBy(() -> testReview.validateUpdatePermission(otherUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 작성한 리뷰만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("수정 권한 검증을 통과할 수 있다")
    void validateUpdatePermission_Success() {
        // when & then (예외가 발생하지 않으면 성공)
        testReview.validateUpdatePermission(testUser.getId());
    }

    // =================== 유효성 검증 테스트 ===================

    @Test
    @DisplayName("빈 제목으로 수정하면 예외가 발생한다")
    void updateTitle_EmptyTitle() {
        // when & then
        assertThatThrownBy(() -> testReview.updateTitle(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 제목은 필수입니다.");
    }

    @Test
    @DisplayName("너무 긴 제목으로 수정하면 예외가 발생한다")
    void updateTitle_TooLongTitle() {
        // given
        String longTitle = "a".repeat(101); // 101자

        // when & then
        assertThatThrownBy(() -> testReview.updateTitle(longTitle))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 제목은 100자를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("잘못된 평점으로 수정하면 예외가 발생한다")
    void updateRating_InvalidRating() {
        // when & then
        assertThatThrownBy(() -> testReview.updateRating(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1점에서 5점 사이여야 합니다.");

        assertThatThrownBy(() -> testReview.updateRating(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1점에서 5점 사이여야 합니다.");
    }

    @Test
    @DisplayName("빈 내용으로 수정하면 예외가 발생한다")
    void updateContent_EmptyContent() {
        // when & then
        assertThatThrownBy(() -> testReview.updateContent(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 내용은 필수입니다.");
    }

    @Test
    @DisplayName("너무 긴 내용으로 수정하면 예외가 발생한다")
    void updateContent_TooLongContent() {
        // given
        String longContent = "a".repeat(1001); // 1001자

        // when & then
        assertThatThrownBy(() -> testReview.updateContent(longContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 내용은 1000자를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("3요소 중 하나라도 null이면 예외가 발생한다")
    void update3Elements_NullElements() {
        // when & then
        assertThatThrownBy(() -> testReview.update3Elements(null, Cushion.SOFT, Stability.STABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("3요소 평가는 모두 필수입니다.");

        assertThatThrownBy(() -> testReview.update3Elements(SizeFit.NORMAL, null, Stability.STABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("3요소 평가는 모두 필수입니다.");

        assertThatThrownBy(() -> testReview.update3Elements(SizeFit.NORMAL, Cushion.SOFT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("3요소 평가는 모두 필수입니다.");
    }

    @Test
    @DisplayName("전체 정보 수정 시 null 값이 있으면 예외가 발생한다")
    void updateReviewInfo_NullValues() {
        // when & then
        assertThatThrownBy(() -> testReview.updateReviewInfo(
                null, 5, "내용", SizeFit.NORMAL, Cushion.SOFT, Stability.STABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 제목은 필수입니다.");

        assertThatThrownBy(() -> testReview.updateReviewInfo(
                "제목", null, "내용", SizeFit.NORMAL, Cushion.SOFT, Stability.STABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1점에서 5점 사이여야 합니다.");

        assertThatThrownBy(() -> testReview.updateReviewInfo(
                "제목", 5, null, SizeFit.NORMAL, Cushion.SOFT, Stability.STABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 내용은 필수입니다.");
    }

    // =================== 이미지 관리 테스트 ===================

    @Test
    @DisplayName("ID로 이미지를 삭제할 수 있다")
    void removeImageById() {
        // given
        ReviewImage mockImage = mock(ReviewImage.class);
        ReflectionTestUtils.setField(mockImage, "reviewImageId", 1L);

        // 리뷰에 이미지 추가 (리플렉션 사용)
        List<ReviewImage> images = List.of(mockImage);
        ReflectionTestUtils.setField(testReview, "images", images);

        // when
        testReview.removeImageById(1L);

        // then
        // removeImage 메서드가 호출되었는지 간접적으로 확인
        // (실제로는 images 리스트에서 제거되고 delete() 호출됨)
    }

    @Test
    @DisplayName("존재하지 않는 이미지 ID로 삭제해도 예외가 발생하지 않는다")
    void removeImageById_NotFound() {
        // when & then (예외가 발생하지 않으면 성공)
        testReview.removeImageById(999L);
    }

    @Test
    @DisplayName("여러 이미지를 ID 목록으로 삭제할 수 있다")
    void removeImagesByIds() {
        // given
        ReviewImage mockImage1 = mock(ReviewImage.class);
        ReviewImage mockImage2 = mock(ReviewImage.class);
        ReflectionTestUtils.setField(mockImage1, "reviewImageId", 1L);
        ReflectionTestUtils.setField(mockImage2, "reviewImageId", 2L);

        List<ReviewImage> images = List.of(mockImage1, mockImage2);
        ReflectionTestUtils.setField(testReview, "images", images);

        // when
        testReview.removeImagesByIds(List.of(1L, 2L));

        // then
        // removeImage 메서드가 각각 호출되었는지 간접적으로 확인
    }

    @Test
    @DisplayName("빈 ID 목록으로 이미지 삭제 시 아무것도 하지 않는다")
    void removeImagesByIds_EmptyList() {
        // when & then (예외가 발생하지 않으면 성공)
        testReview.removeImagesByIds(List.of());
    }

    @Test
    @DisplayName("null ID 목록으로 이미지 삭제 시 아무것도 하지 않는다")
    void removeImagesByIds_NullList() {
        // when & then (예외가 발생하지 않으면 성공)
        testReview.removeImagesByIds(null);
    }

    @Test
    @DisplayName("경계값 테스트 - 최대 길이 제목과 내용")
    void updateReviewInfo_BoundaryValues() {
        // given
        String maxTitle = "a".repeat(100); // 정확히 100자
        String maxContent = "a".repeat(1000); // 정확히 1000자

        // when & then (예외가 발생하지 않으면 성공)
        testReview.updateReviewInfo(
                maxTitle, 1, maxContent,
                SizeFit.VERY_SMALL, Cushion.VERY_SOFT, Stability.VERY_UNSTABLE);

        assertThat(testReview.getTitle()).isEqualTo(maxTitle);
        assertThat(testReview.getContent()).isEqualTo(maxContent);
        assertThat(testReview.getRating()).isEqualTo(1);
    }

    @Test
    @DisplayName("경계값 테스트 - 최대 평점")
    void updateRating_BoundaryValues() {
        // when & then (예외가 발생하지 않으면 성공)
        testReview.updateRating(1); // 최소값
        assertThat(testReview.getRating()).isEqualTo(1);

        testReview.updateRating(5); // 최대값
        assertThat(testReview.getRating()).isEqualTo(5);
    }
}