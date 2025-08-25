package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.response.WishListResponse;
import com.cMall.feedShop.cart.application.dto.response.info.WishlistInfo;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.DiscountType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * WishlistService 테스트
 * 기존 CartServiceTest와 OrderServiceTest 패턴을 따라 구현
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService 테스트")
class WishlistGetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private WishlistService wishlistService;

    // 테스트 데이터
    private User testUser;
    private List<WishlistInfo> testWishlistInfos;
    private Page<WishlistInfo> testWishlistPage;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 1. 테스트용 사용자 생성
        testUser = createTestUser();

        // 2. 테스트용 찜한 상품 정보 생성
        testWishlistInfos = createTestWishlistInfos();

        // 3. 테스트용 페이지 생성
        testWishlistPage = new PageImpl<>(testWishlistInfos, PageRequest.of(0, 10), testWishlistInfos.size());
    }

    // ==================== getWishList 테스트 ====================

    @Test
    @DisplayName("찜한 상품 목록 조회 성공")
    void getWishList_Success() {
        // given
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(testUser));
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(2L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when
        WishListResponse response = wishlistService.getWishList(0, 10, "testUser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWishlists()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isHasNext()).isFalse();

        // 메서드 호출 검증
        verify(userRepository).findByLoginId("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 성공 - 빈 목록")
    void getWishList_Success_EmptyList() {
        // given
        Page<WishlistInfo> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(testUser));
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(0L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(emptyPage);

        // when
        WishListResponse response = wishlistService.getWishList(0, 10, "testUser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWishlists()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.isHasNext()).isFalse();

        verify(userRepository).findByLoginId("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 실패 - 사용자 없음")
    void getWishList_Fail_UserNotFound() {
        // given
        given(userRepository.findByLoginId("nonExistentUser")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.getWishList(0, 10, "nonExistentUser"))
                .isInstanceOf(CartException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByLoginId("nonExistentUser");
        verify(wishlistRepository, times(0)).countWishlistByUserId(any());
        verify(wishlistRepository, times(0)).findWishlistByUserId(any(), any());
    }

    @Test
    @DisplayName("페이지 파라미터 정규화 테스트 - 음수 페이지")
    void getWishList_PageParameterNormalization_NegativePage() {
        // given
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(testUser));
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(5L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when - 음수 페이지는 0으로 정규화
        WishListResponse response = wishlistService.getWishList(-1, 10, "testUser");

        // then
        assertThat(response).isNotNull();
        verify(userRepository).findByLoginId("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지 파라미터 정규화 테스트 - 큰 페이지 크기")
    void getWishList_PageParameterNormalization_LargeSize() {
        // given
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(testUser));
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(5L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when - 큰 사이즈는 PagingUtils에서 정규화됨
        WishListResponse response = wishlistService.getWishList(0, 200, "testUser");

        // then
        assertThat(response).isNotNull();
        verify(userRepository).findByLoginId("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지 오버플로우 방지 테스트")
    void getWishList_PageOverflowPrevention() {
        // given - 총 5개 데이터, 페이지 크기 10, 요청 페이지 5 (존재하지 않는 페이지)
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(testUser));
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(5L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when - 페이지 5 요청 (실제로는 0으로 조정됨)
        WishListResponse response = wishlistService.getWishList(5, 10, "testUser");

        // then
        assertThat(response).isNotNull();
        verify(userRepository).findByLoginId("testUser");
        verify(wishlistRepository).countWishlistByUserId(1L);
        verify(wishlistRepository).findWishlistByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("찜한 상품 데이터 검증 테스트")
    void getWishList_DataValidation() {
        // given
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(testUser));
        given(wishlistRepository.countWishlistByUserId(1L)).willReturn(2L);
        given(wishlistRepository.findWishlistByUserId(eq(1L), any(Pageable.class))).willReturn(testWishlistPage);

        // when
        WishListResponse response = wishlistService.getWishList(0, 10, "testUser");

        // then - 첫 번째 찜한 상품 데이터 검증
        WishlistInfo firstWishlist = response.getWishlists().get(0);
        assertThat(firstWishlist.getWishlistId()).isEqualTo(1L);
        assertThat(firstWishlist.getProductId()).isEqualTo(100L);
        assertThat(firstWishlist.getProductName()).isEqualTo("나이키 에어맥스");
        assertThat(firstWishlist.getProductImageUrl()).isEqualTo("nike-image.jpg");
        assertThat(firstWishlist.getProductPrice()).isEqualTo(new BigDecimal("150000"));
        assertThat(firstWishlist.getDiscountType()).isEqualTo(DiscountType.RATE_DISCOUNT);
        assertThat(firstWishlist.getDiscountValue()).isEqualTo(new BigDecimal("10"));
        assertThat(firstWishlist.getCreatedAt()).isNotNull();

        // 두 번째 찜한 상품 데이터 검증
        WishlistInfo secondWishlist = response.getWishlists().get(1);
        assertThat(secondWishlist.getWishlistId()).isEqualTo(2L);
        assertThat(secondWishlist.getProductId()).isEqualTo(200L);
        assertThat(secondWishlist.getProductName()).isEqualTo("아디다스 스탠스미스");
        assertThat(secondWishlist.getDiscountType()).isEqualTo(DiscountType.FIXED_DISCOUNT);
    }

    // ==================== 테스트 데이터 생성 헬퍼 메서드들 ====================

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser() {
        User user = new User("testUser", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    /**
     * 테스트용 찜한 상품 정보 리스트 생성
     */
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