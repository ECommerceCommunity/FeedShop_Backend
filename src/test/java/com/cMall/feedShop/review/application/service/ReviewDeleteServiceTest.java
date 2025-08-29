package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.review.application.dto.response.ReviewDeleteResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewImageDeleteResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.exception.ReviewAccessDeniedException;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@DisplayName("ReviewDeleteService 삭제 기능 테스트")
class ReviewDeleteServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ReviewImageService reviewImageService;
    
    @Mock
    private ReviewImageRepository reviewImageRepository;
    
    @Mock
    private StorageService gcpStorageService;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ReviewDeleteService reviewDeleteService;

    private User testUser;
    private Review testReview;
    private ReviewImage testReviewImage;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
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
        
        testReview = Review.builder()
                .title("테스트 리뷰")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("테스트 내용")
                .user(testUser)
                .product(testProduct)
                .build();
        
        // Reflection을 사용하여 reviewId 설정
        try {
            java.lang.reflect.Field reviewIdField = Review.class.getDeclaredField("reviewId");
            reviewIdField.setAccessible(true);
            reviewIdField.set(testReview, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set reviewId", e);
        }
        
        testReviewImage = ReviewImage.builder()
                .review(testReview)
                .originalFilename("test.jpg")
                .storedFilename("stored_test.jpg")
                .filePath("/uploads/reviews/test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(0)
                .build();
        
        // Reflection을 사용하여 reviewImageId 설정
        try {
            java.lang.reflect.Field imageIdField = ReviewImage.class.getDeclaredField("reviewImageId");
            imageIdField.setAccessible(true);
            imageIdField.set(testReviewImage, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set reviewImageId", e);
        }
    }

    @Test
    @DisplayName("리뷰를 성공적으로 삭제할 수 있다")
    void deleteReview_Success() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewImageRepository.findByReviewReviewIdAndDeletedFalse(1L))
                .willReturn(Arrays.asList(testReviewImage));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // when
        ReviewDeleteResponse response = reviewDeleteService.deleteReview(1L);

        // then
        assertThat(response.getDeletedReviewId()).isEqualTo(1L);
        // GCP Storage가 없는 환경에서도 DB에서는 삭제 처리됨
        assertThat(response.getDeletedImageCount()).isEqualTo(1);
        
        verify(reviewRepository).findById(1L);
        verify(reviewImageRepository).findByReviewReviewIdAndDeletedFalse(1L);
        verify(reviewRepository).save(testReview);
        verify(reviewImageRepository).saveAll(any());
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 삭제 시 예외가 발생한다")
    void deleteReview_ReviewNotFound_ThrowsException() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewDeleteService.deleteReview(999L))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("ID 999에 해당하는 리뷰를 찾을 수 없습니다");
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("다른 사용자의 리뷰 삭제 시 예외가 발생한다")
    void deleteReview_AccessDenied_ThrowsException() {
        // given
        User otherUser = new User(2L, "otheruser", "password", "other@example.com", UserRole.USER);
        mockSecurityContextForUser(otherUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

        // when & then
        assertThatThrownBy(() -> reviewDeleteService.deleteReview(1L))
                .isInstanceOf(ReviewAccessDeniedException.class);
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 이미지를 일괄 삭제할 수 있다")
    void deleteReviewImages_Success() {
        // given
        mockSecurityContext();
        List<Long> imageIds = Arrays.asList(1L, 2L);
        List<Long> deletedImageIds = Arrays.asList(1L, 2L);
        
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewImageService.deleteSelectedImages(1L, imageIds)).willReturn(deletedImageIds);
        given(reviewImageService.getActiveImageCount(1L)).willReturn(0);

        // when
        ReviewImageDeleteResponse response = reviewDeleteService.deleteReviewImages(1L, imageIds);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getDeletedImageIds()).hasSize(2);
        assertThat(response.getRemainingImageCount()).isEqualTo(0);
        
        verify(reviewImageService).deleteSelectedImages(1L, imageIds);
        verify(reviewImageService).getActiveImageCount(1L);
    }

    @Test
    @DisplayName("리뷰 이미지를 개별 삭제할 수 있다")
    void deleteReviewImage_Success() {
        // given
        mockSecurityContext();
        Long imageId = 1L;
        List<Long> deletedImageIds = Arrays.asList(imageId);
        
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewImageService.deleteSelectedImages(1L, Arrays.asList(imageId))).willReturn(deletedImageIds);
        given(reviewImageService.getActiveImageCount(1L)).willReturn(1);

        // when
        ReviewImageDeleteResponse response = reviewDeleteService.deleteReviewImage(1L, imageId);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getRemainingImageCount()).isEqualTo(1);
        
        verify(reviewImageService).deleteSelectedImages(1L, Arrays.asList(imageId));
        verify(reviewImageService).getActiveImageCount(1L);
    }

    @Test
    @DisplayName("존재하지 않는 이미지 삭제 시 예외가 발생한다")
    void deleteReviewImage_ImageNotFound_ThrowsException() {
        // given
        mockSecurityContext();
        Long imageId = 999L;
        
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewImageService.deleteSelectedImages(1L, Arrays.asList(imageId))).willReturn(Arrays.asList());

        // when & then
        assertThatThrownBy(() -> reviewDeleteService.deleteReviewImage(1L, imageId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("삭제할 이미지를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("리뷰의 모든 이미지를 삭제할 수 있다")
    void deleteAllReviewImages_Success() {
        // given
        mockSecurityContext();
        
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewImageRepository.findByReviewReviewIdAndDeletedFalse(1L))
                .willReturn(Arrays.asList(testReviewImage, testReviewImage, testReviewImage));
        
        // 실제로 호출되는 인자와 일치하도록 수정: testReviewImage의 ID는 모두 1L
        List<Long> actualImageIds = Arrays.asList(1L, 1L, 1L);
        given(reviewImageService.deleteSelectedImages(1L, actualImageIds)).willReturn(actualImageIds);

        // when
        ReviewImageDeleteResponse response = reviewDeleteService.deleteAllReviewImages(1L);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getDeletedImageIds()).hasSize(3);
        
        verify(reviewImageRepository).findByReviewReviewIdAndDeletedFalse(1L);
        verify(reviewImageService).deleteSelectedImages(1L, actualImageIds);
    }

    @Test
    @DisplayName("삭제할 이미지가 없는 경우 빈 목록을 반환한다")
    void deleteAllReviewImages_NoImages_ReturnsEmptyList() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewImageRepository.findByReviewReviewIdAndDeletedFalse(1L)).willReturn(Arrays.asList());

        // when
        ReviewImageDeleteResponse response = reviewDeleteService.deleteAllReviewImages(1L);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getDeletedImageIds()).isEmpty();
        
        verify(reviewImageService, never()).deleteSelectedImages(anyLong(), any());
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 리뷰 삭제 시 예외가 발생한다")
    void deleteReview_UnauthenticatedUser_ThrowsException() {
        // given
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> reviewDeleteService.deleteReview(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("로그인이 필요합니다");
        
        verify(reviewRepository, never()).findById(anyLong());
    }

    private void mockSecurityContext() {
        mockSecurityContextForUser(testUser);
    }

    private void mockSecurityContextForUser(User user) {
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUsername()).willReturn(user.getEmail());
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
    }
}