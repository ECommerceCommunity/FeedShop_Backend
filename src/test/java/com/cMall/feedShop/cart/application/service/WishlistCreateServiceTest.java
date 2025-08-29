package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.WishListRequest;
import com.cMall.feedShop.cart.application.dto.response.WishListCreateResponse;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.enums.DiscountType;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistCreateService 찜 등록 기능 테스트")
class WishlistCreateServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistHelper wishlistHelper;

    @InjectMocks
    private WishlistCreateService wishlistCreateService;

    private User testUser;
    private Product testProduct;
    private Store testStore;
    private Category testCategory;
    private WishListRequest testRequest;
    private WishList savedWishList;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testUser = new User("testLogin", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        testProduct = Product.builder()
                .name("나이키 에어맥스")
                .price(new BigDecimal("150000"))
                .store(testStore)
                .category(testCategory)
                .discountType(DiscountType.NONE)
                .description("편안한 운동화")
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        testRequest = new WishListRequest(1L);

        savedWishList = WishList.builder()
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(savedWishList, "wishlistId", 1L);
        ReflectionTestUtils.setField(savedWishList, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("찜 등록 성공")
    void addWishList_Success() {
        // given
        given(wishlistHelper.getCurrentUser("testLogin")).willReturn(testUser);
        given(wishlistHelper.getProduct(1L)).willReturn(testProduct);
        given(wishlistRepository.save(any(WishList.class))).willReturn(savedWishList);

        // when
        WishListCreateResponse response = wishlistCreateService.addWishList(testRequest, "testLogin");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWishlistId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getCreatedAt()).isNotNull();

        verify(wishlistHelper, times(1)).getCurrentUser("testLogin");
        verify(wishlistHelper, times(1)).getProduct(1L);
        verify(wishlistRepository, times(1)).save(any(WishList.class));
        verify(wishlistRepository, times(1)).increaseWishCount(1L);
    }

    @Test
    @DisplayName("찜 등록 실패 - 사용자 없음")
    void addWishList_Fail_UserNotFound() {
        // given
        given(wishlistHelper.getCurrentUser("nonExistentUser")).willThrow(new CartException(ErrorCode.USER_NOT_FOUND));

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistCreateService.addWishList(testRequest, "nonExistentUser")
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(wishlistHelper, times(1)).getCurrentUser("nonExistentUser");
        verify(wishlistHelper, never()).getProduct(any());
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).increaseWishCount(any());
    }

    @Test
    @DisplayName("찜 등록 실패 - 상품 없음")
    void addWishList_Fail_ProductNotFound() {
        // given
        given(wishlistHelper.getCurrentUser("testLogin")).willReturn(testUser);
        given(wishlistHelper.getProduct(999L)).willThrow(new CartException(ErrorCode.PRODUCT_NOT_FOUND));

        WishListRequest invalidRequest = new WishListRequest(999L);

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistCreateService.addWishList(invalidRequest, "testLogin")
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(wishlistHelper, times(1)).getCurrentUser("testLogin");
        verify(wishlistHelper, times(1)).getProduct(999L);
        verify(wishlistRepository, never()).save(any());
        verify(wishlistRepository, never()).increaseWishCount(any());
    }

    @Test
    @DisplayName("찜 등록 실패 - 이미 찜한 상품")
    void addWishList_Fail_AlreadyWished() {
        // given
        given(wishlistHelper.getCurrentUser("testLogin")).willReturn(testUser);
        given(wishlistHelper.getProduct(1L)).willReturn(testProduct);
        given(wishlistRepository.existsByUserIdAndProduct_ProductIdAndDeletedAtIsNull(1L, 1L))
                .willReturn(true);

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistCreateService.addWishList(testRequest, "testLogin")
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.ALREADY_WISHED_PRODUCT);
        verify(wishlistHelper, times(1)).getCurrentUser("testLogin");
        verify(wishlistHelper, times(1)).getProduct(1L);
        verify(wishlistRepository, times(1)).existsByUserIdAndProduct_ProductIdAndDeletedAtIsNull(1L, 1L);
        verify(wishlistRepository, never()).save(any(WishList.class));
        verify(wishlistRepository, never()).increaseWishCount(any());
    }

    @Test
    @DisplayName("찜 등록 시 Repository 호출 순서 검증")
    void addWishList_VerifyRepositoryCallOrder() {
        // given
        given(wishlistHelper.getCurrentUser("testLogin")).willReturn(testUser);
        given(wishlistHelper.getProduct(1L)).willReturn(testProduct);
        given(wishlistRepository.save(any(WishList.class))).willReturn(savedWishList);

        // when
        wishlistCreateService.addWishList(testRequest, "testLogin");

        // then
        var inOrder = inOrder(wishlistHelper, wishlistRepository);
        inOrder.verify(wishlistHelper).getCurrentUser("testLogin");
        inOrder.verify(wishlistHelper).getProduct(1L);
        inOrder.verify(wishlistRepository).save(any(WishList.class));
        inOrder.verify(wishlistRepository).increaseWishCount(1L);
    }

    @Test
    @DisplayName("찜 등록 실패 시 상품 찜 수 증가하지 않음 검증")
    void addWishList_VerifyNoIncreaseWishNumberOnFailure() {
        // given
        given(wishlistHelper.getCurrentUser("testLogin")).willReturn(testUser);
        given(wishlistHelper.getProduct(1L)).willReturn(testProduct);
        given(wishlistRepository.existsByUserIdAndProduct_ProductIdAndDeletedAtIsNull(any(), any()))
                .willReturn(true);

        // when & then
        assertThrows(CartException.class, () ->
                wishlistCreateService.addWishList(testRequest, "testLogin")
        );

        verify(wishlistRepository, never()).increaseWishCount(1L);
    }
}