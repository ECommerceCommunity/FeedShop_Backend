package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemUpdateRequest;
import com.cMall.feedShop.cart.application.dto.request.RecentViewRequest;
import com.cMall.feedShop.cart.application.dto.request.WishListRequest;
import com.cMall.feedShop.cart.application.dto.response.CartCreateResponse;
import com.cMall.feedShop.cart.application.dto.response.RecentViewResponse;
import com.cMall.feedShop.cart.application.dto.response.WishListResponse;
import com.cMall.feedShop.cart.domain.model.RecentView;
import com.cMall.feedShop.cart.domain.model.Wishlist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cart 추가 DTO 및 모델 테스트")
class AdditionalCartTests {

    @Test
    @DisplayName("CartItemUpdateRequest 생성 및 getter 테스트")
    void cartItemUpdateRequest_Test() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest();
        ReflectionTestUtils.setField(request, "quantity", 5);

        // when & then
        assertThat(request.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("CartCreateResponse 생성 테스트")
    void cartCreateResponse_Test() {
        // given
        CartCreateResponse response = new CartCreateResponse();
        ReflectionTestUtils.setField(response, "cartId", 1L);

        // when & then
        assertThat(response.getCartId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("RecentViewRequest 생성 및 getter 테스트")
    void recentViewRequest_Test() {
        // given
        RecentViewRequest request = new RecentViewRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "productId", 10L);

        // when & then
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getProductId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("RecentViewResponse 생성 및 getter 테스트")
    void recentViewResponse_Test() {
        // given
        RecentViewResponse response = new RecentViewResponse();
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(response, "viewId", 1L);
        ReflectionTestUtils.setField(response, "productId", 10L);
        ReflectionTestUtils.setField(response, "viewedAt", now);

        // when & then
        assertThat(response.getViewId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getViewedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("WishListRequest 생성 및 getter 테스트")
    void wishListRequest_Test() {
        // given
        WishListRequest request = new WishListRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "productId", 10L);

        // when & then
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getProductId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("WishListResponse 생성 및 getter 테스트")
    void wishListResponse_Test() {
        // given
        WishListResponse response = new WishListResponse();
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(response, "wishlistId", 1L);
        ReflectionTestUtils.setField(response, "productId", 10L);
        ReflectionTestUtils.setField(response, "createdAt", now);

        // when & then
        assertThat(response.getWishlistId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("RecentView 엔티티 생성 테스트")
    void recentView_Entity_Test() {
        // given
        RecentView recentView = new RecentView();
        LocalDateTime now = LocalDateTime.now();

        ReflectionTestUtils.setField(recentView, "viewId", 1L);
        ReflectionTestUtils.setField(recentView, "userId", 1L);
        ReflectionTestUtils.setField(recentView, "productId", 10L);
        ReflectionTestUtils.setField(recentView, "viewedAt", now);
        ReflectionTestUtils.setField(recentView, "createdAt", now);

        // when & then (getter가 없으므로 리플렉션으로 확인)
        assertThat(ReflectionTestUtils.getField(recentView, "viewId")).isEqualTo(1L);
        assertThat(ReflectionTestUtils.getField(recentView, "userId")).isEqualTo(1L);
        assertThat(ReflectionTestUtils.getField(recentView, "productId")).isEqualTo(10L);
        assertThat(ReflectionTestUtils.getField(recentView, "viewedAt")).isEqualTo(now);
        assertThat(ReflectionTestUtils.getField(recentView, "createdAt")).isEqualTo(now);
    }

    @Test
    @DisplayName("Wishlist 엔티티 생성 테스트")
    void wishlist_Entity_Test() {
        // given
        Wishlist wishlist = new Wishlist();
        LocalDateTime now = LocalDateTime.now();

        ReflectionTestUtils.setField(wishlist, "wishlistId", 1L);
        ReflectionTestUtils.setField(wishlist, "userId", 1L);
        ReflectionTestUtils.setField(wishlist, "productId", 10L);
        ReflectionTestUtils.setField(wishlist, "createdAt", now);

        // when & then (getter가 없으므로 리플렉션으로 확인)
        assertThat(ReflectionTestUtils.getField(wishlist, "wishlistId")).isEqualTo(1L);
        assertThat(ReflectionTestUtils.getField(wishlist, "userId")).isEqualTo(1L);
        assertThat(ReflectionTestUtils.getField(wishlist, "productId")).isEqualTo(10L);
        assertThat(ReflectionTestUtils.getField(wishlist, "createdAt")).isEqualTo(now);
    }

    @Test
    @DisplayName("빈 DTO 객체 생성 테스트")
    void empty_DTOs_Test() {
        // given & when
        CartItemUpdateRequest updateRequest = new CartItemUpdateRequest();
        RecentViewRequest recentViewRequest = new RecentViewRequest();
        WishListRequest wishListRequest = new WishListRequest();
        CartCreateResponse createResponse = new CartCreateResponse();
        RecentViewResponse recentViewResponse = new RecentViewResponse();
        WishListResponse wishListResponse = new WishListResponse();

        // then - null 필드도 정상적으로 동작하는지 확인
        assertThat(updateRequest.getQuantity()).isNull();
        assertThat(recentViewRequest.getUserId()).isNull();
        assertThat(recentViewRequest.getProductId()).isNull();
        assertThat(wishListRequest.getUserId()).isNull();
        assertThat(wishListRequest.getProductId()).isNull();
        assertThat(createResponse.getCartId()).isNull();
        assertThat(recentViewResponse.getViewId()).isNull();
        assertThat(wishListResponse.getWishlistId()).isNull();
    }
}