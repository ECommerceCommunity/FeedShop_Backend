package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.review.domain.service.ReviewDuplicationValidator;
import com.cMall.feedShop.review.domain.service.ReviewPurchaseVerificationService;
import com.cMall.feedShop.user.application.service.BadgeService;
import com.cMall.feedShop.user.application.service.PointService;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewCreateService 테스트")
class ReviewCreateServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ReviewDuplicationValidator duplicationValidator;
    
    @Mock
    private ReviewPurchaseVerificationService purchaseVerificationService;
    
    @Mock
    private ReviewImageService reviewImageService;
    
    @Mock
    private ReviewImageRepository reviewImageRepository;
    
    @Mock
    private BadgeService badgeService;
    
    @Mock
    private UserLevelService userLevelService;
    
    @Mock
    private PointService pointService;
    
    @Mock
    private StorageService gcpStorageService;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ReviewCreateService reviewCreateService;

    private User testUser;
    private Product testProduct;
    private ReviewCreateRequest createRequest;
    private Review savedReview;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        // testUser의 loginId 설정 (UUID 형식)
        try {
            java.lang.reflect.Field loginIdField = User.class.getDeclaredField("loginId");
            loginIdField.setAccessible(true);
            loginIdField.set(testUser, "befb3068-0fdc-4cb1-9096-6fbd2e2ec8c8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set loginId", e);
        }
        Store testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        
        Category testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        
        testProduct = Product.builder()
                .name("테스트 신발")
                .price(BigDecimal.valueOf(100000))
                .store(testStore)
                .category(testCategory)
                .description("테스트 설명")
                .build();
        
        // Reflection을 사용하여 productId 설정
        try {
            java.lang.reflect.Field productIdField = Product.class.getDeclaredField("productId");
            productIdField.setAccessible(true);
            productIdField.set(testProduct, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set productId", e);
        }
        
        createRequest = ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();
                
        savedReview = Review.builder()
                .title(createRequest.getTitle())
                .rating(createRequest.getRating())
                .sizeFit(createRequest.getSizeFit())
                .cushion(createRequest.getCushion())
                .stability(createRequest.getStability())
                .content(createRequest.getContent())
                .user(testUser)
                .product(testProduct)
                .build();
        
        // Reflection을 사용하여 reviewId 설정
        try {
            java.lang.reflect.Field reviewIdField = Review.class.getDeclaredField("reviewId");
            reviewIdField.setAccessible(true);
            reviewIdField.set(savedReview, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set reviewId", e);
        }
    }

    @Test
    @DisplayName("이미지 없이 리뷰를 성공적으로 생성할 수 있다")
    void createReview_WithoutImages_Success() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
        
        // when
        ReviewCreateResponse response = reviewCreateService.createReview(createRequest, null);
        
        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
        assertThat(response.getImageUrls()).isEmpty();
        assertThat(response.getPointsEarned()).isEqualTo(100);
        
        verify(duplicationValidator).validateNoDuplicateActiveReview(testUser.getId(), testProduct.getProductId());
        verify(purchaseVerificationService).validateUserPurchasedProduct(testUser, testProduct.getProductId());
        verify(reviewRepository).save(any(Review.class));
        verify(pointService).earnPoints(testUser, 100, "리뷰 작성 보상", 1L);
        verify(badgeService).checkAndAwardReviewBadges(testUser.getId(), 1L);
        verify(userLevelService).recordActivity(testUser.getId(), ActivityType.REVIEW_CREATION, "리뷰 작성", 1L, "REVIEW");
    }

    @Test
    @DisplayName("이미지와 함께 리뷰를 성공적으로 생성할 수 있다")
    void createReview_WithImages_Success() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
        
        List<MultipartFile> images = Arrays.asList(
                new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "image1".getBytes()),
                new MockMultipartFile("image2", "image2.jpg", "image/jpeg", "image2".getBytes())
        );
        
        List<UploadResult> uploadResults = Arrays.asList(
                UploadResult.builder()
                        .originalFilename("image1.jpg")
                        .storedFilename("stored_image1.jpg")
                        .filePath("/uploads/reviews/image1.jpg")
                        .fileSize(1024L)
                        .contentType("image/jpeg")
                        .build(),
                UploadResult.builder()
                        .originalFilename("image2.jpg")
                        .storedFilename("stored_image2.jpg")
                        .filePath("/uploads/reviews/image2.jpg")
                        .fileSize(2048L)
                        .contentType("image/jpeg")
                        .build()
        );
        
        // GCP Storage 서비스가 주입되도록 설정
        reviewCreateService = new ReviewCreateService(
                reviewRepository, userRepository, productRepository, duplicationValidator,
                purchaseVerificationService, reviewImageService, reviewImageRepository,
                badgeService, userLevelService, pointService
        );
        
        // Reflection을 사용하여 gcpStorageService 주입
        try {
            java.lang.reflect.Field gcpStorageServiceField = ReviewCreateService.class.getDeclaredField("gcpStorageService");
            gcpStorageServiceField.setAccessible(true);
            gcpStorageServiceField.set(reviewCreateService, gcpStorageService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject gcpStorageService", e);
        }
        
        given(gcpStorageService.uploadFilesWithDetails(images, UploadDirectory.REVIEWS)).willReturn(uploadResults);
        
        // when
        ReviewCreateResponse response = reviewCreateService.createReview(createRequest, images);
        
        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
        assertThat(response.getImageUrls()).hasSize(2);
        assertThat(response.getImageUrls()).containsExactly("/uploads/reviews/image1.jpg", "/uploads/reviews/image2.jpg");
        assertThat(response.getPointsEarned()).isEqualTo(100);
        
        verify(gcpStorageService).uploadFilesWithDetails(images, UploadDirectory.REVIEWS);
        verify(reviewImageService).saveReviewImages(savedReview, images);
    }

    @Test
    @DisplayName("존재하지 않는 상품으로 리뷰 생성 시 예외가 발생한다")
    void createReview_ProductNotFound_ThrowsException() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> reviewCreateService.createReview(createRequest, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("중복 리뷰 검증 실패 시 예외가 발생한다")
    void createReview_DuplicateReview_ThrowsException() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        doThrow(new IllegalStateException("이미 리뷰를 작성하셨습니다"))
                .when(duplicationValidator).validateNoDuplicateActiveReview(testUser.getId(), testProduct.getProductId());
        
        // when & then
        assertThatThrownBy(() -> reviewCreateService.createReview(createRequest, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 리뷰를 작성하셨습니다");
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("구매 이력 검증 실패 시 예외가 발생한다")
    void createReview_PurchaseVerificationFailed_ThrowsException() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        doThrow(new IllegalStateException("구매 이력이 없습니다"))
                .when(purchaseVerificationService).validateUserPurchasedProduct(testUser, testProduct.getProductId());
        
        // when & then
        assertThatThrownBy(() -> reviewCreateService.createReview(createRequest, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("구매 이력이 없습니다");
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 리뷰 생성 시 예외가 발생한다")
    void createReview_UnauthenticatedUser_ThrowsException() {
        // given
        given(userRepository.findByLoginId("befb3068-0fdc-4cb1-9096-6fbd2e2ec8c8")).willReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUsername()).willReturn("befb3068-0fdc-4cb1-9096-6fbd2e2ec8c8");
        
        // when & then
        assertThatThrownBy(() -> reviewCreateService.createReview(createRequest, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("이미지 업로드 실패 시에도 리뷰는 성공적으로 생성된다")
    void createReview_ImageUploadFails_ReviewStillCreated() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
        
        List<MultipartFile> images = Arrays.asList(
                new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "image1".getBytes())
        );
        
        // GCP Storage 서비스가 주입되도록 설정
        reviewCreateService = new ReviewCreateService(
                reviewRepository, userRepository, productRepository, duplicationValidator,
                purchaseVerificationService, reviewImageService, reviewImageRepository,
                badgeService, userLevelService, pointService
        );
        
        // Reflection을 사용하여 gcpStorageService 주입
        try {
            java.lang.reflect.Field gcpStorageServiceField = ReviewCreateService.class.getDeclaredField("gcpStorageService");
            gcpStorageServiceField.setAccessible(true);
            gcpStorageServiceField.set(reviewCreateService, gcpStorageService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject gcpStorageService", e);
        }
        
        given(gcpStorageService.uploadFilesWithDetails(images, UploadDirectory.REVIEWS))
                .willThrow(new RuntimeException("Storage upload failed"));
        
        // when
        ReviewCreateResponse response = reviewCreateService.createReview(createRequest, images);
        
        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
        assertThat(response.getImageUrls()).isEmpty(); // 업로드 실패로 빈 목록
        assertThat(response.getPointsEarned()).isEqualTo(100);
        
        verify(reviewRepository).save(any(Review.class));
        verify(reviewImageService).saveReviewImages(savedReview, images); // 로컬 저장은 계속 진행
    }

    @Test
    @DisplayName("포인트 적립 실패 시에도 리뷰는 성공적으로 생성된다")
    void createReview_PointAwardFails_ReviewStillCreated() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
        doThrow(new RuntimeException("Point service failed"))
                .when(pointService).earnPoints(testUser, 100, "리뷰 작성 보상", 1L);
        
        // when
        ReviewCreateResponse response = reviewCreateService.createReview(createRequest, null);
        
        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
        assertThat(response.getPointsEarned()).isEqualTo(0); // 포인트 적립 실패로 0
        
        verify(reviewRepository).save(any(Review.class));
        verify(badgeService).checkAndAwardReviewBadges(testUser.getId(), 1L); // 뱃지 체크는 계속 진행
    }

    @Test
    @DisplayName("뱃지 수여 실패 시에도 리뷰는 성공적으로 생성된다")
    void createReview_BadgeAwardFails_ReviewStillCreated() {
        // given
        mockSecurityContext();
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
        doThrow(new RuntimeException("Badge service failed"))
                .when(badgeService).checkAndAwardReviewBadges(testUser.getId(), 1L);
        
        // when
        ReviewCreateResponse response = reviewCreateService.createReview(createRequest, null);
        
        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
        assertThat(response.getPointsEarned()).isEqualTo(100);
        
        verify(reviewRepository).save(any(Review.class));
        verify(pointService).earnPoints(testUser, 100, "리뷰 작성 보상", 1L);
    }

    private void mockSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUsername()).willReturn("befb3068-0fdc-4cb1-9096-6fbd2e2ec8c8");
        given(userRepository.findByLoginId("befb3068-0fdc-4cb1-9096-6fbd2e2ec8c8")).willReturn(Optional.of(testUser));
    }
}