package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.response.WishListResponse;
import com.cMall.feedShop.cart.application.dto.response.info.WishlistInfo;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistReadService 테스트")
class WishlistReadServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistHelper wishlistHelper;

    @InjectMocks
    private WishlistReadService wishlistReadService;

    private User testUser;
    private List<WishlistInfo> testWishlistInfos;
    private Page<WishlistInfo> testWishlistPage;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testUser = createTestUser();
        testWishlistInfos = createTestWishlistInfos();
        testWishlistPage = new PageImpl<>(testWishlistInfos, PageRequest.of(0, 10), testWishlistInfos.size());
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 성공")
    void getWishList_Success() {
        // given
        given(wishlistHelper.getCurrentUser("testUser")).willReturn(testUser);
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(2L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when
        WishListResponse response = wishlistReadService.getWishList(0, 10, "testUser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWishlists()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isHasNext()).isFalse();

        verify(wishlistHelper).getCurrentUser("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 성공 - 빈 목록")
    void getWishList_Success_EmptyList() {
        // given
        Page<WishlistInfo> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        given(wishlistHelper.getCurrentUser("testUser")).willReturn(testUser);
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(0L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(emptyPage);

        // when
        WishListResponse response = wishlistReadService.getWishList(0, 10, "testUser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWishlists()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.isHasNext()).isFalse();

        verify(wishlistHelper).getCurrentUser("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 실패 - 사용자 없음")
    void getWishList_Fail_UserNotFound() {
        // given
        given(wishlistHelper.getCurrentUser("nonExistentUser")).willThrow(new CartException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> wishlistReadService.getWishList(0, 10, "nonExistentUser"))
                .isInstanceOf(CartException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(wishlistHelper).getCurrentUser("nonExistentUser");
        verify(wishlistRepository, times(0)).countWishlistByUserId(any());
        verify(wishlistRepository, times(0)).findWishlistByUserId(any(), any());
    }

    @Test
    @DisplayName("페이지 파라미터 정규화 테스트 - 음수 페이지")
    void getWishList_PageParameterNormalization_NegativePage() {
        // given
        given(wishlistHelper.getCurrentUser("testUser")).willReturn(testUser);
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(5L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when
        WishListResponse response = wishlistReadService.getWishList(-1, 10, "testUser");

        // then
        assertThat(response).isNotNull();
        verify(wishlistHelper).getCurrentUser("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지 파라미터 정규화 테스트 - 큰 페이지 크기")
    void getWishList_PageParameterNormalization_LargeSize() {
        // given
        given(wishlistHelper.getCurrentUser("testUser")).willReturn(testUser);
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(5L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when
        WishListResponse response = wishlistReadService.getWishList(0, 200, "testUser");

        // then
        assertThat(response).isNotNull();
        verify(wishlistHelper).getCurrentUser("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("찜한 상품 데이터 검증 테스트")
    void getWishList_DataValidation() {
        // given
        given(wishlistHelper.getCurrentUser("testUser")).willReturn(testUser);
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(2L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when
        WishListResponse response = wishlistReadService.getWishList(0, 10, "testUser");

        // then
        WishlistInfo firstWishlist = response.getWishlists().get(0);
        assertThat(firstWishlist.getWishlistId()).isEqualTo(1L);
        assertThat(firstWishlist.getProductId()).isEqualTo(100L);
        assertThat(firstWishlist.getProductName()).isEqualTo("나이키 에어맥스");
        assertThat(firstWishlist.getProductImageUrl()).isEqualTo("nike-image.jpg");
        assertThat(firstWishlist.getProductPrice()).isEqualTo(new BigDecimal("150000"));
        assertThat(firstWishlist.getDiscountType()).isEqualTo(DiscountType.RATE_DISCOUNT);
        assertThat(firstWishlist.getDiscountValue()).isEqualTo(new BigDecimal("10"));
        assertThat(firstWishlist.getCreatedAt()).isNotNull();
    }

    private User createTestUser() {
        User user = new User("testUser", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private List<WishlistInfo> createTestWishlistInfos() {
        LocalDateTime now = LocalDateTime.now();

        WishlistInfo wishlist1 = new WishlistInfo(
                1L,
                100L,
                "나이키 에어맥스",
                "nike-image.jpg",
                new BigDecimal("150000"),
                DiscountType.RATE_DISCOUNT,
                new BigDecimal("10"),
                now.minusHours(2)
        );

        WishlistInfo wishlist2 = new WishlistInfo(
                2L,
                200L,
                "아디다스 스탠스미스",
                "adidas-image.jpg",
                new BigDecimal("120000"),
                DiscountType.FIXED_DISCOUNT,
                new BigDecimal("20000"),
                now.minusHours(1)
        );

        return Arrays.asList(wishlist1, wishlist2);
    }
}