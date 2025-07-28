package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private UserProfile testUserProfile;
    private Review testReview;
    private ReviewCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testLogin", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testUserProfile = new UserProfile(testUser, "테스트사용자", "테스트닉네임", "010-1234-5678");
        testUser.setUserProfile(testUserProfile);

        testReview = Review.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .user(testUser)
                .productId(1L)
                .build();
        ReflectionTestUtils.setField(testReview, "reviewId", 1L);
        ReflectionTestUtils.setField(testReview, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testReview, "updatedAt", LocalDateTime.now());

        createRequest = new ReviewCreateRequest();
        createRequest.setTitle("좋은 신발입니다");
        createRequest.setRating(5);
        createRequest.setSizeFit(SizeFit.NORMAL);
        createRequest.setCushion(Cushion.SOFT);
        createRequest.setStability(Stability.STABLE);
        createRequest.setContent("정말 편하고 좋습니다. 추천해요!");
        createRequest.setProductId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("리뷰를 성공적으로 생성할 수 있다")
    void createReviewSuccessfully() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(reviewRepository.save(any(Review.class))).willReturn(testReview);

            // when
            ReviewCreateResponse response = reviewService.createReview(createRequest);

            // then
            assertThat(response.getReviewId()).isEqualTo(1L);
            assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 작성되었습니다.");
            verify(reviewRepository, times(1)).save(any(Review.class));
        }
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 리뷰를 생성하려 하면 예외가 발생한다")
    void createReviewWithUnauthenticatedUser() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            given(securityContext.getAuthentication()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("로그인이 필요합니다");
        }
    }

    @Test
    @DisplayName("상품별 리뷰 목록을 성공적으로 조회할 수 있다")
    void getProductReviewsSuccessfully() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);

        given(reviewRepository.findActiveReviewsByProductId(1L, PageRequest.of(0, 20)))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);

        // when
        ReviewListResponse response = reviewService.getProductReviews(1L, 0, 20, "latest");

        // then
        assertThat(response.getReviews()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getTotalReviews()).isEqualTo(10L);
    }

    @Test
    @DisplayName("리뷰 상세 정보를 성공적으로 조회할 수 있다")
    void getReviewSuccessfully() {
        // given
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

        // when
        ReviewResponse response = reviewService.getReview(1L);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("좋은 신발입니다");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getUserName()).isEqualTo("테스트사용자");
    }

    @Test
    @DisplayName("존재하지 않는 리뷰를 조회하면 예외가 발생한다")
    void getReviewNotFound() {
        // given
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.getReview(999L))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("리뷰를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
    void createReviewWithNonExistentUser() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(createRequest))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Test
    @DisplayName("평균 평점이 null인 경우 0.0을 반환한다")
    void getProductReviewsWithNullAverageRating() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);

        given(reviewRepository.findActiveReviewsByProductId(1L, PageRequest.of(0, 20)))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(null); // null 반환
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);

        // when
        ReviewListResponse response = reviewService.getProductReviews(1L, 0, 20, "latest");

        // then
        assertThat(response.getAverageRating()).isEqualTo(0.0);
    }

    private void mockSecurityContext() {
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getName()).willReturn("test@test.com");
    }
}