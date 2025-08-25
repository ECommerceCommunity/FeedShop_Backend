package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.storage.GcpStorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewImageResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.domain.exception.DuplicateReviewException;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.review.domain.service.ReviewDuplicationValidator;
import com.cMall.feedShop.review.domain.service.ReviewPurchaseVerificationService;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트 (DTO 불변성 적용)")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private ReviewDuplicationValidator duplicationValidator;

    @Mock
    private ReviewPurchaseVerificationService purchaseVerificationService;

    @Mock
    private ReviewImageService reviewImageService;
    
    @Mock
    private com.cMall.feedShop.review.domain.repository.ReviewImageRepository reviewImageRepository;

    @Mock
    private GcpStorageService gcpStorageService;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private UserProfile testUserProfile;
    private Product testProduct;
    private Store testStore;
    private Category testCategory;
    private Review testReview;
    private ReviewCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testLogin", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        UserProfile testUserProfile = UserProfile.builder()
                .user(testUser)
                .name("테스트사용자")
                .nickname("테스트닉네임")
                .phone("010-1234-5678")
                // 다른 필드들 (birthDate, height, footSize, profileImageUrl)도 필요에 따라 추가
                .birthDate(LocalDate.of(1990, 1, 1))
                .height(175)
                .footSize(270)
                .profileImageUrl("https://test-image.com/profile.jpg")
                .build();
        testUser.setUserProfile(testUserProfile);

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

        testReview = Review.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(testReview, "reviewId", 1L);
        ReflectionTestUtils.setField(testReview, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testReview, "updatedAt", LocalDateTime.now());

        // ✅ Builder 패턴으로 불변 DTO 생성
        createRequest = ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();

        ReflectionTestUtils.setField(reviewService, "gcpStorageService", gcpStorageService);
        
        // 구매이력검증 기본 Mock 설정 (모든 테스트에서 검증 통과하도록)
        lenient().doNothing().when(purchaseVerificationService).validateUserPurchasedProduct(any(User.class), any(Long.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("리뷰를 성공적으로 생성할 수 있다 (불변 DTO)")
    void createReviewSuccessfully() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
            given(reviewRepository.save(any(Review.class))).willReturn(testReview);

            // when
            ReviewCreateResponse response = reviewService.createReview(createRequest, null);

            // then
            assertThat(response.getReviewId()).isEqualTo(1L);
            assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
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
            assertThatThrownBy(() -> reviewService.createReview(createRequest, null))
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
            assertThatThrownBy(() -> reviewService.createReview(createRequest, null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 예외가 발생한다")
    void createReviewWithNonExistentProduct() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(createRequest, null))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
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
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(null);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);

        // when
        ReviewListResponse response = reviewService.getProductReviews(1L, 0, 20, "latest");

        // then
        assertThat(response.getAverageRating()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("이미 리뷰를 작성한 상품에 중복 리뷰를 작성하면 예외가 발생한다")
    void createDuplicateReview() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

            // 중복 검증에서 예외 발생하도록 설정
            doThrow(new DuplicateReviewException(1L))
                    .when(duplicationValidator).validateNoDuplicateActiveReview(1L, 1L);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(createRequest, null))
                    .isInstanceOf(DuplicateReviewException.class)
                    .hasMessageContaining("상품 ID 1에 대한 리뷰를 이미 작성하셨습니다");

            verify(duplicationValidator, times(1)).validateNoDuplicateActiveReview(1L, 1L);
        }
    }

    @Test
    @DisplayName("중복 리뷰가 없으면 정상적으로 리뷰를 생성할 수 있다")
    void createReviewWithNoDuplicate() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
            given(reviewRepository.save(any(Review.class))).willReturn(testReview);

            // 중복 검증 통과하도록 설정 (예외 발생 안함)
            doNothing().when(duplicationValidator).validateNoDuplicateActiveReview(1L, 1L);

            // when
            ReviewCreateResponse response = reviewService.createReview(createRequest, null);

            // then
            assertThat(response.getReviewId()).isEqualTo(1L);
            assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
            verify(duplicationValidator, times(1)).validateNoDuplicateActiveReview(1L, 1L);
            verify(reviewRepository, times(1)).save(any(Review.class));
        }
    }

    @Test
    @DisplayName("이미지와 함께 리뷰를 성공적으로 생성할 수 있다")
    void createReviewWithImages() {
        // given
        MultipartFile imageFile = mock(MultipartFile.class);
        List<MultipartFile> imageFiles = List.of(imageFile);

        UploadResult mockResult = mock(UploadResult.class);
        given(mockResult.getOriginalFilename()).willReturn("image.jpg");
        given(mockResult.getStoredFilename()).willReturn("uuid-image.jpg");
        given(mockResult.getFilePath()).willReturn("reviews/uuid-image.jpg");
        given(mockResult.getFileSize()).willReturn(12345L);
        given(mockResult.getContentType()).willReturn("image/jpeg");

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
            given(reviewRepository.save(any(Review.class))).willReturn(testReview);
            given(gcpStorageService.uploadFilesWithDetails(any(List.class), eq(UploadDirectory.REVIEWS)))
                    .willReturn(List.of(mockResult));

            // when
            ReviewCreateResponse response = reviewService.createReview(createRequest, imageFiles);

            // then
            assertThat(response.getReviewId()).isEqualTo(1L);
            assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
            verify(reviewRepository, times(1)).save(any(Review.class));
            verify(gcpStorageService, times(1)).uploadFilesWithDetails(any(List.class), eq(UploadDirectory.REVIEWS));
        }
    }

    @Test
    @DisplayName("이미지 없이 리뷰를 생성할 수 있다")
    void createReviewWithoutImages() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
            given(reviewRepository.save(any(Review.class))).willReturn(testReview);

            // when
            ReviewCreateResponse response = reviewService.createReview(createRequest, null);

            // then
            assertThat(response.getReviewId()).isEqualTo(1L);
            verify(reviewRepository, times(1)).save(any(Review.class));
            verify(reviewImageService, never()).saveReviewImages(any(), any());
        }
    }

    @Test
    @DisplayName("리뷰 상세 조회 시 이미지 정보가 포함된다")
    void getReviewWithImages() {
        // given
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

        ReviewImageResponse imageResponse = ReviewImageResponse.builder()
                .reviewImageId(1L)
                .originalFilename("test-image.jpg")
                .imageUrl("http://localhost:8080/uploads/reviews/test-image.jpg")
                .imageOrder(1)
                .fileSize(1024L)
                .build();

        given(reviewImageService.getReviewImages(1L)).willReturn(List.of(imageResponse));

        // when
        ReviewResponse response = reviewService.getReview(1L);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.isHasImages()).isTrue();
        assertThat(response.getImages()).hasSize(1);
        assertThat(response.getImages().get(0).getOriginalFilename()).isEqualTo("test-image.jpg");
        verify(reviewImageService, times(1)).getReviewImages(1L);
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 시 이미지 정보가 포함된다")
    void getProductReviewsWithImages() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);

        given(reviewRepository.findActiveReviewsByProductId(1L, PageRequest.of(0, 20)))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);

        ReviewImageResponse imageResponse = ReviewImageResponse.builder()
                .reviewImageId(1L)
                .originalFilename("test-image.jpg")
                .imageUrl("http://localhost:8080/uploads/reviews/test-image.jpg")
                .imageOrder(1)
                .fileSize(1024L)
                .build();

        given(reviewImageService.getReviewImages(1L)).willReturn(List.of(imageResponse));

        // when
        ReviewListResponse response = reviewService.getProductReviews(1L, 0, 20, "latest");

        // then
        assertThat(response.getReviews()).hasSize(1);
        ReviewResponse reviewResponse = response.getReviews().get(0);
        assertThat(reviewResponse.isHasImages()).isTrue();
        assertThat(reviewResponse.getImages()).hasSize(1);
        verify(reviewImageService, times(1)).getReviewImages(1L);
    }

    @Test
    @DisplayName("빈 이미지 리스트로 리뷰를 생성할 수 있다")
    void createReviewWithEmptyImageList() {
        // given
        List<MultipartFile> emptyImageList = List.of();

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
            given(reviewRepository.save(any(Review.class))).willReturn(testReview);

            // when
            ReviewCreateResponse response = reviewService.createReview(createRequest, emptyImageList);

            // then
            assertThat(response.getReviewId()).isEqualTo(1L);
            verify(reviewRepository, times(1)).save(any(Review.class));
            verify(reviewImageService, never()).saveReviewImages(any(), any());
        }
    }

    @Test
    @DisplayName("이미지가 없는 리뷰 상세 조회")
    void getReviewWithoutImages() {
        // given
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewImageService.getReviewImages(1L)).willReturn(List.of()); // 빈 이미지 리스트

        // when
        ReviewResponse response = reviewService.getReview(1L);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.isHasImages()).isFalse();
        assertThat(response.getImages()).isEmpty();
        verify(reviewImageService, times(1)).getReviewImages(1L);
    }

    @Test
    @DisplayName("불변 DTO의 필드 값이 올바르게 Review 엔티티에 전달된다")
    void immutableDtoFieldsTransferCorrectly() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockSecurityContext();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
            given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
            given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
                Review savedReview = invocation.getArgument(0);
                // DTO의 값들이 올바르게 전달되었는지 검증
                assertThat(savedReview.getTitle()).isEqualTo(createRequest.getTitle());
                assertThat(savedReview.getRating()).isEqualTo(createRequest.getRating());
                assertThat(savedReview.getSizeFit()).isEqualTo(createRequest.getSizeFit());
                assertThat(savedReview.getCushion()).isEqualTo(createRequest.getCushion());
                assertThat(savedReview.getStability()).isEqualTo(createRequest.getStability());
                assertThat(savedReview.getContent()).isEqualTo(createRequest.getContent());
                return testReview;
            });

            // when
            reviewService.createReview(createRequest, null);

            // then
            verify(reviewRepository, times(1)).save(any(Review.class));
        }
    }

    private void mockSecurityContext() {
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getName()).willReturn("test@test.com");
        given(authentication.getPrincipal()).willReturn("test@test.com"); // String으로 설정하면 getName()이 호출됨
    }

    // ========== 필터링 메서드 테스트들 ==========

    @Test
    @DisplayName("필터링된 리뷰 목록을 조회할 수 있다 - 평점 필터")
    void getProductReviewsWithFilters_RatingFilter() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);
        
        given(reviewRepository.findActiveReviewsByProductIdWithFilters(
                eq(1L), eq(5), isNull(), isNull(), isNull(), any()))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);
        given(reviewImageService.getReviewImages(1L)).willReturn(List.of());

        // when
        ReviewListResponse response = reviewService.getProductReviewsWithFilters(
                1L, 0, 20, "latest", 5, null, null, null);

        // then
        assertThat(response.getReviews()).hasSize(1);
        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getTotalReviews()).isEqualTo(10L);
        verify(reviewRepository).findActiveReviewsByProductIdWithFilters(
                eq(1L), eq(5), isNull(), isNull(), isNull(), any());
    }

    @Test
    @DisplayName("필터링된 리뷰 목록을 조회할 수 있다 - 3Element 필터")
    void getProductReviewsWithFilters_3ElementFilter() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);
        
        given(reviewRepository.findActiveReviewsByProductIdWithFilters(
                eq(1L), isNull(), eq(SizeFit.NORMAL), eq(Cushion.SOFT), eq(Stability.STABLE), any()))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);
        given(reviewImageService.getReviewImages(1L)).willReturn(List.of());

        // when
        ReviewListResponse response = reviewService.getProductReviewsWithFilters(
                1L, 0, 20, "latest", null, "NORMAL", "SOFT", "STABLE");

        // then
        assertThat(response.getReviews()).hasSize(1);
        verify(reviewRepository).findActiveReviewsByProductIdWithFilters(
                eq(1L), isNull(), eq(SizeFit.NORMAL), eq(Cushion.SOFT), eq(Stability.STABLE), any());
    }

    @Test
    @DisplayName("필터링된 리뷰 목록을 조회할 수 있다 - 복합 필터")
    void getProductReviewsWithFilters_CombinedFilters() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);
        
        given(reviewRepository.findActiveReviewsByProductIdWithFilters(
                eq(1L), eq(5), eq(SizeFit.NORMAL), eq(Cushion.SOFT), isNull(), any()))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);
        given(reviewImageService.getReviewImages(1L)).willReturn(List.of());

        // when
        ReviewListResponse response = reviewService.getProductReviewsWithFilters(
                1L, 0, 20, "latest", 5, "NORMAL", "SOFT", null);

        // then
        assertThat(response.getReviews()).hasSize(1);
        verify(reviewRepository).findActiveReviewsByProductIdWithFilters(
                eq(1L), eq(5), eq(SizeFit.NORMAL), eq(Cushion.SOFT), isNull(), any());
    }

    @Test
    @DisplayName("필터링된 리뷰 목록을 조회할 수 있다 - 모든 필터가 null")
    void getProductReviewsWithFilters_NoFilters() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);
        
        given(reviewRepository.findActiveReviewsByProductIdWithFilters(
                eq(1L), isNull(), isNull(), isNull(), isNull(), any()))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);
        given(reviewImageService.getReviewImages(1L)).willReturn(List.of());

        // when
        ReviewListResponse response = reviewService.getProductReviewsWithFilters(
                1L, 0, 20, "latest", null, null, null, null);

        // then
        assertThat(response.getReviews()).hasSize(1);
        verify(reviewRepository).findActiveReviewsByProductIdWithFilters(
                eq(1L), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    @DisplayName("잘못된 enum 값으로 필터링 시 예외가 발생한다")
    void getProductReviewsWithFilters_InvalidEnumValue() {
        // when & then
        assertThatThrownBy(() -> 
                reviewService.getProductReviewsWithFilters(
                        1L, 0, 20, "latest", null, "INVALID_SIZE", null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("잘못된 필터 값입니다");
    }

    @Test
    @DisplayName("페이지 크기가 범위를 벗어나면 기본값으로 설정된다")
    void getProductReviewsWithFilters_InvalidPageSize() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);
        
        given(reviewRepository.findActiveReviewsByProductIdWithFilters(
                eq(1L), isNull(), isNull(), isNull(), isNull(), any()))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);
        given(reviewImageService.getReviewImages(1L)).willReturn(List.of());

        // when - 잘못된 페이지 크기 (0, 200)
        ReviewListResponse response1 = reviewService.getProductReviewsWithFilters(
                1L, 0, 0, "latest", null, null, null, null);
        ReviewListResponse response2 = reviewService.getProductReviewsWithFilters(
                1L, 0, 200, "latest", null, null, null, null);

        // then - 기본값 20으로 설정됨
        assertThat(response1.getSize()).isEqualTo(20);
        assertThat(response2.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("음수 페이지 번호는 0으로 설정된다")
    void getProductReviewsWithFilters_NegativePageNumber() {
        // given
        List<Review> reviews = List.of(testReview);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 20), 1);
        
        given(reviewRepository.findActiveReviewsByProductIdWithFilters(
                eq(1L), isNull(), isNull(), isNull(), isNull(), any()))
                .willReturn(reviewPage);
        given(reviewRepository.findAverageRatingByProductId(1L)).willReturn(4.5);
        given(reviewRepository.countActiveReviewsByProductId(1L)).willReturn(10L);
        given(reviewImageService.getReviewImages(1L)).willReturn(List.of());

        // when
        ReviewListResponse response = reviewService.getProductReviewsWithFilters(
                1L, -5, 20, "latest", null, null, null, null);

        // then
        assertThat(response.getNumber()).isEqualTo(0);
    }
}