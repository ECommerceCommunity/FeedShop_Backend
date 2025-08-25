package com.cMall.feedShop.review.domain.service;

import com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse;
import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
import com.cMall.feedShop.review.domain.exception.ReviewPurchaseRequiredException;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewPurchaseVerificationServiceTest {

    @Mock
    private PurchasedItemService purchasedItemService;

    @InjectMocks
    private ReviewPurchaseVerificationService reviewPurchaseVerificationService;

    private User testUser;
    private Long productId;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password123", "test@example.com", UserRole.USER);
        productId = 100L;
    }

    @Test
    @DisplayName("구매한 상품의 경우 검증 통과")
    void validateUserPurchasedProduct_Success_WhenUserHasPurchasedProduct() {
        // given
        List<PurchasedItemInfo> purchasedItems = Arrays.asList(
            createPurchasedItemInfo(1L, productId, "구매한 상품"),
            createPurchasedItemInfo(2L, 200L, "다른 상품")
        );
        
        PurchasedItemListResponse response = PurchasedItemListResponse.from(purchasedItems);
        when(purchasedItemService.getPurchasedItems(testUser.getLoginId())).thenReturn(response);

        // when & then
        assertThatCode(() -> {
            reviewPurchaseVerificationService.validateUserPurchasedProduct(testUser, productId);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("구매하지 않은 상품의 경우 예외 발생")
    void validateUserPurchasedProduct_ThrowsException_WhenUserHasNotPurchasedProduct() {
        // given
        List<PurchasedItemInfo> purchasedItems = Arrays.asList(
            createPurchasedItemInfo(2L, 200L, "다른 상품1"),
            createPurchasedItemInfo(3L, 300L, "다른 상품2")
        );
        
        PurchasedItemListResponse response = PurchasedItemListResponse.from(purchasedItems);
        when(purchasedItemService.getPurchasedItems(testUser.getLoginId())).thenReturn(response);

        // when & then
        assertThatThrownBy(() -> {
            reviewPurchaseVerificationService.validateUserPurchasedProduct(testUser, productId);
        }).isInstanceOf(ReviewPurchaseRequiredException.class)
          .hasMessage("구매한 상품에만 리뷰를 작성할 수 있습니다.");
    }

    @Test
    @DisplayName("구매이력이 없는 경우 예외 발생")
    void validateUserPurchasedProduct_ThrowsException_WhenUserHasNoPurchaseHistory() {
        // given
        PurchasedItemListResponse response = PurchasedItemListResponse.from(Collections.emptyList());
        when(purchasedItemService.getPurchasedItems(testUser.getLoginId())).thenReturn(response);

        // when & then
        assertThatThrownBy(() -> {
            reviewPurchaseVerificationService.validateUserPurchasedProduct(testUser, productId);
        }).isInstanceOf(ReviewPurchaseRequiredException.class)
          .hasMessage("구매한 상품에만 리뷰를 작성할 수 있습니다.");
    }

    @Test
    @DisplayName("구매이력 조회 중 오류 발생 시 예외 처리")
    void validateUserPurchasedProduct_ThrowsException_WhenServiceThrowsException() {
        // given
        when(purchasedItemService.getPurchasedItems(anyString())).thenThrow(new RuntimeException("서비스 오류"));

        // when & then
        assertThatThrownBy(() -> {
            reviewPurchaseVerificationService.validateUserPurchasedProduct(testUser, productId);
        }).isInstanceOf(ReviewPurchaseRequiredException.class)
          .hasMessage("구매이력 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    private PurchasedItemInfo createPurchasedItemInfo(Long orderItemId, Long productId, String productName) {
        return new PurchasedItemInfo(
                orderItemId,
                productId,
                productName,
                "image_url.jpg",
                LocalDateTime.now()
        );
    }
}