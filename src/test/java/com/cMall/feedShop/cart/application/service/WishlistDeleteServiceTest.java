package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService 테스트")
class WishlistDeleteServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    // 테스트 데이터
    private User testUser;
    private Product testProduct;
    private WishList testWishList;
    private Store testStore;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // User 설정
        testUser = new User("testuser", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // Store 설정
        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        // Category 설정
        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        // Product 설정
        testProduct = Product.builder()
                .name("테스트 운동화")
                .price(new BigDecimal("50000"))
                .store(testStore)
                .category(testCategory)
                .discountType(DiscountType.NONE)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        // WishList 설정
        testWishList = WishList.builder()
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(testWishList, "wishlistId", 1L);
    }

    // ==================== deleteWishList 성공 테스트 ====================

    @Test
    @DisplayName("찜한 상품 취소 성공")
    void deleteWishList_Success() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(testUser));
        given(wishlistRepository.findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.of(testWishList));
        given(wishlistRepository.save(any(WishList.class))).willReturn(testWishList);

        // when
        wishlistService.deleteWishList(productId, loginId);

        // then
        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, times(1)).save(testWishList);
        verify(wishlistRepository, times(1)).decreaseWishCount(productId);

        // deletedAt이 설정되었는지 확인 (간접적으로 delete 메서드가 호출되었는지 확인)
        assertThat(testWishList.getDeletedAt()).isNotNull();
    }

    // ==================== deleteWishList 실패 테스트 ====================

    @Test
    @DisplayName("찜한 상품 취소 실패 - 사용자 없음")
    void deleteWishList_Fail_UserNotFound() {
        // given
        Long productId = 1L;
        String loginId = "nonexistent";

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(wishlistRepository, never()).findByUserIdAndProductIdAndDeletedAtIsNull(any(), any());
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    @Test
    @DisplayName("찜한 상품 취소 실패 - 찜한 상품 없음")
    void deleteWishList_Fail_WishlistNotFound() {
        // given
        Long productId = 999L;
        String loginId = "testuser";

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(testUser));
        given(wishlistRepository.findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    @Test
    @DisplayName("찜한 상품 취소 실패 - 다른 사용자의 찜한 상품")
    void deleteWishList_Fail_DifferentUser() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        // 다른 사용자
        User otherUser = new User("otheruser", "password123", "other@test.com", UserRole.USER);
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(testUser));
        given(wishlistRepository.findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.empty()); // 현재 사용자의 찜 목록에는 없음

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    // ==================== 경계값 테스트 ====================

    @Test
    @DisplayName("찜한 상품 취소 - null 매개변수 테스트")
    void deleteWishList_NullParameters() {
        // when & then - productId가 null인 경우
        assertThrows(Exception.class, () ->
                wishlistService.deleteWishList(null, "testuser"));

        // when & then - loginId가 null인 경우
        assertThrows(Exception.class, () ->
                wishlistService.deleteWishList(1L, null));
    }

    @Test
    @DisplayName("찜한 상품 취소 - 빈 문자열 loginId")
    void deleteWishList_EmptyLoginId() {
        // given
        Long productId = 1L;
        String emptyLoginId = "";

        given(userRepository.findByLoginId(emptyLoginId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistService.deleteWishList(productId, emptyLoginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("찜한 상품 취소 - 이미 삭제된 찜 상품")
    void deleteWishList_AlreadyDeleted() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        // 이미 삭제된 찜 상품 (deletedAt이 설정됨)
        WishList deletedWishList = WishList.builder()
                .user(testUser)
                .product(testProduct)
                .build();
        deletedWishList.delete(); // 이미 삭제됨
        ReflectionTestUtils.setField(deletedWishList, "wishlistId", 1L);

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(testUser));
        given(wishlistRepository.findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.empty()); // 이미 삭제된 경우 조회되지 않음

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    // ==================== 통합 테스트 ====================

    @Test
    @DisplayName("찜한 상품 취소 - 전체 플로우 검증")
    void deleteWishList_FullFlow() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        // 삭제 전 상태 확인
        assertThat(testWishList.getDeletedAt()).isNull();

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(testUser));
        given(wishlistRepository.findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.of(testWishList));
        given(wishlistRepository.save(any(WishList.class))).willAnswer(invocation -> {
            WishList savedWishList = invocation.getArgument(0);
            // deletedAt이 설정되었는지 확인 (시간 비교 대신 null 체크)
            assertThat(savedWishList.getDeletedAt()).isNotNull();
            return savedWishList;
        });

        // when
        wishlistService.deleteWishList(productId, loginId);

        // then - 메서드 호출 순서와 횟수 검증
        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, times(1)).save(testWishList);
        verify(wishlistRepository, times(1)).decreaseWishCount(productId);

        // deletedAt이 설정되었는지 최종 확인
        assertThat(testWishList.getDeletedAt()).isNotNull();
    }
}