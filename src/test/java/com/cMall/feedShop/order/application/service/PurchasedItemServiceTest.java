package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse;
import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.infrastructure.repository.OrderItemQueryRepository;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchasedItemService 단위 테스트")
class PurchasedItemServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemQueryRepository orderItemQueryRepository;


    @InjectMocks
    private PurchasedItemService purchasedItemService;

    private User user;
    private List<PurchasedItemInfo> purchasedItems;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "password", "test@test.com", UserRole.USER);
        user.setId(1L);

        purchasedItems = Arrays.asList(
                PurchasedItemInfo.of(1L, 1L, "상품1", "http://image1.jpg", LocalDateTime.now()),
                PurchasedItemInfo.of(2L, 2L, "상품2", "http://image2.jpg", LocalDateTime.now().minusDays(1))
        );
    }

    @Test
    @DisplayName("구매 상품 목록 조회 성공")
    void getPurchasedItems_Success() {
        // Given
        when(userRepository.findByLoginId("testUser")).thenReturn(Optional.of(user));
        when(orderItemQueryRepository.findPurchasedItemsByUserId(1L)).thenReturn(purchasedItems);

        // When
        PurchasedItemListResponse result = purchasedItemService.getPurchasedItems("testUser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getItems().get(0).getProductName()).isEqualTo("상품1");
        assertThat(result.getItems().get(1).getProductName()).isEqualTo("상품2");
    }

    @Test
    @DisplayName("구매 상품 목록 조회 성공 - 빈 목록")
    void getPurchasedItems_EmptyList() {
        // Given
        when(userRepository.findByLoginId("testUser")).thenReturn(Optional.of(user));
        when(orderItemQueryRepository.findPurchasedItemsByUserId(1L)).thenReturn(Arrays.asList());

        // When
        PurchasedItemListResponse result = purchasedItemService.getPurchasedItems("testUser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("구매 상품 목록 조회 실패 - 사용자 없음")
    void getPurchasedItems_UserNotFound() {
        // Given
        when(userRepository.findByLoginId("testUser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchasedItemService.getPurchasedItems("testUser"))
                .isInstanceOf(UserException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}