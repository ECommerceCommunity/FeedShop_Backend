package com.cMall.feedShop.review.application.dto.response;

import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.user.domain.enums.FootWidth;
import com.cMall.feedShop.user.domain.enums.Gender;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ReviewResponse DTO 테스트")
class ReviewResponseTest {

    @Test
    @DisplayName("사용자 신체 정보가 있는 리뷰를 ReviewResponse로 변환할 수 있다")
    void convertReviewWithUserBodyInfoToResponse() {
        // given
        User user = createUserWithProfile();
        Product product = createProduct();
        Review review = createReview(user, product);

        // when
        ReviewResponse response = ReviewResponse.from(review, List.of());

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("좋은 신발입니다");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("홍길동");
        
        // 사용자 신체 정보 검증
        assertThat(response.getUserHeight()).isEqualTo(175);
        assertThat(response.getUserWeight()).isEqualTo(70);
        assertThat(response.getUserFootSize()).isEqualTo(270);
        assertThat(response.getUserFootWidth()).isEqualTo("NORMAL");
    }

    @Test
    @DisplayName("사용자 프로필이 없는 경우 신체 정보는 null이고 이름은 익명으로 설정된다")
    void convertReviewWithoutUserProfileToResponse() {
        // given
        User user = createUserWithoutProfile();
        Product product = createProduct();
        Review review = createReview(user, product);

        // when
        ReviewResponse response = ReviewResponse.from(review, List.of());

        // then
        assertThat(response.getUserName()).isEqualTo("익명");
        assertThat(response.getUserHeight()).isNull();
        assertThat(response.getUserWeight()).isNull();
        assertThat(response.getUserFootSize()).isNull();
        assertThat(response.getUserFootWidth()).isNull();
    }

    @Test
    @DisplayName("발 폭 정보가 없는 경우 null로 설정된다")
    void convertReviewWithoutFootWidthToResponse() {
        // given
        User user = createUserWithProfileButNoFootWidth();
        Product product = createProduct();
        Review review = createReview(user, product);

        // when
        ReviewResponse response = ReviewResponse.from(review, List.of());

        // then
        assertThat(response.getUserName()).isEqualTo("홍길동");
        assertThat(response.getUserHeight()).isEqualTo(175);
        assertThat(response.getUserWeight()).isEqualTo(70);
        assertThat(response.getUserFootSize()).isEqualTo(270);
        assertThat(response.getUserFootWidth()).isNull(); // 발 폭 정보 없음
    }

    private User createUserWithProfile() {
        User user = new User(1L, "hong@test.com", "password", "hong@test.com", UserRole.USER);
        
        UserProfile profile = UserProfile.builder()
                .user(user)
                .name("홍길동")
                .nickname("길동이")
                .phone("01012345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .height(175)
                .weight(70)
                .footSize(270)
                .footWidth(FootWidth.NORMAL)
                .build();
                
        user.setUserProfile(profile);
        return user;
    }

    private User createUserWithoutProfile() {
        return new User(2L, "noProfile@test.com", "password", "noProfile@test.com", UserRole.USER);
    }

    private User createUserWithProfileButNoFootWidth() {
        User user = new User(3L, "noFootWidth@test.com", "password", "noFootWidth@test.com", UserRole.USER);
        
        UserProfile profile = UserProfile.builder()
                .user(user)
                .name("홍길동")
                .nickname("길동이")
                .phone("01012345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .height(175)
                .weight(70)
                .footSize(270)
                .footWidth(null) // 발 폭 정보 없음
                .build();
                
        user.setUserProfile(profile);
        return user;
    }

    private Product createProduct() {
        // Mock을 사용하여 간단하게 Product 생성
        Product product = mock(Product.class);
        when(product.getProductId()).thenReturn(1L);
        when(product.getName()).thenReturn("테스트 신발");
        return product;
    }

    private Review createReview(User user, Product product) {
        Review review = Review.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋네요")
                .user(user)
                .product(product)
                .build();
                
        // reviewId를 수동으로 설정 (테스트용)
        try {
            java.lang.reflect.Field field = Review.class.getDeclaredField("reviewId");
            field.setAccessible(true);
            field.set(review, 1L);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
        
        return review;
    }
}