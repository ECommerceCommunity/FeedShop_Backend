package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistDeleteService 테스트")
class WishlistDeleteServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistHelper wishlistHelper;

    @InjectMocks
    private WishlistDeleteService wishlistDeleteService;

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
        testUser = new User("testuser", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        testProduct = Product.builder()
                .name("테스트 운동화")
                .price(new BigDecimal("50000"))
                .store(testStore)
                .category(testCategory)
                .discountType(DiscountType.NONE)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        testWishList = WishList.builder()
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(testWishList, "wishlistId", 1L);
    }

    @Test
    @DisplayName("찜한 상품 취소 성공")
    void deleteWishList_Success() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        given(wishlistHelper.getCurrentUser(loginId)).willReturn(testUser);
        given(wishlistRepository.findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.of(testWishList));
        given(wishlistRepository.save(any(WishList.class))).willReturn(testWishList);

        // when
        wishlistDeleteService.deleteWishList(productId, loginId);

        // then
        verify(wishlistHelper, times(1)).getCurrentUser(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, times(1)).save(testWishList);
        verify(wishlistRepository, times(1)).decreaseWishCount(productId);

        assertThat(testWishList.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("찜한 상품 취소 실패 - 사용자 없음")
    void deleteWishList_Fail_UserNotFound() {
        // given
        Long productId = 1L;
        String loginId = "nonexistent";

        given(wishlistHelper.getCurrentUser(loginId)).willThrow(new CartException(ErrorCode.USER_NOT_FOUND));

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistDeleteService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(wishlistHelper, times(1)).getCurrentUser(loginId);
        verify(wishlistRepository, never()).findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(any(), any());
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    @Test
    @DisplayName("찜한 상품 취소 실패 - 찜한 상품 없음")
    void deleteWishList_Fail_WishlistNotFound() {
        // given
        Long productId = 999L;
        String loginId = "testuser";

        given(wishlistHelper.getCurrentUser(loginId)).willReturn(testUser);
        given(wishlistRepository.findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistDeleteService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        verify(wishlistHelper, times(1)).getCurrentUser(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    @Test
    @DisplayName("찜한 상품 취소 실패 - 다른 사용자의 찜한 상품")
    void deleteWishList_Fail_DifferentUser() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        User otherUser = new User("otheruser", "password123", "other@test.com", UserRole.USER);
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        given(wishlistHelper.getCurrentUser(loginId)).willReturn(testUser);
        given(wishlistRepository.findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistDeleteService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        verify(wishlistHelper, times(1)).getCurrentUser(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    @Test
    @DisplayName("찜한 상품 취소 - 이미 삭제된 찜 상품")
    void deleteWishList_AlreadyDeleted() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        given(wishlistHelper.getCurrentUser(loginId)).willReturn(testUser);
        given(wishlistRepository.findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistDeleteService.deleteWishList(productId, loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        verify(wishlistHelper, times(1)).getCurrentUser(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).decreaseWishCount(any());
    }

    @Test
    @DisplayName("찜한 상품 취소 - 전체 플로우 검증")
    void deleteWishList_FullFlow() {
        // given
        Long productId = 1L;
        String loginId = "testuser";

        assertThat(testWishList.getDeletedAt()).isNull();

        given(wishlistHelper.getCurrentUser(loginId)).willReturn(testUser);
        given(wishlistRepository.findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId))
                .willReturn(Optional.of(testWishList));
        given(wishlistRepository.save(any(WishList.class))).willAnswer(invocation -> {
            WishList savedWishList = invocation.getArgument(0);
            assertThat(savedWishList.getDeletedAt()).isNotNull();
            return savedWishList;
        });

        // when
        wishlistDeleteService.deleteWishList(productId, loginId);

        // then
        verify(wishlistHelper, times(1)).getCurrentUser(loginId);
        verify(wishlistRepository, times(1)).findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(testUser.getId(), productId);
        verify(wishlistRepository, times(1)).save(testWishList);
        verify(wishlistRepository, times(1)).decreaseWishCount(productId);

        assertThat(testWishList.getDeletedAt()).isNotNull();
    }
}