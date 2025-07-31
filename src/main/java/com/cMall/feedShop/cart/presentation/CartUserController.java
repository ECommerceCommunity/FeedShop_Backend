package com.cMall.feedShop.cart.presentation;

import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.request.CartItemUpdateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.application.service.CartService;
import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class CartUserController {

    private final CartService cartService;

    /**
     * 장바구니에 상품 추가
     * POST /api/users/cart/items
     */
    @PostMapping("/cart/items")
    @ApiResponseFormat(message = "상품이 장바구니에 추가되었습니다.")
    public ApiResponse<CartItemResponse> addCartItem(
            @Valid @RequestBody CartItemCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CartItemResponse data = cartService.addCartItem(request, userDetails);
        return ApiResponse.success(data);
    }

    /**
     * 장바구니 목록 조회
     * GET /api/users/cart
     */
    @GetMapping("/cart")
    @ApiResponseFormat(message = "장바구니 목록을 성공적으로 조회했습니다.")
    public ApiResponse<CartItemListResponse> getCartItems(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CartItemListResponse data = cartService.getCartItems(userDetails);
        return ApiResponse.success(data);
    }

    /**
     * 장바구니 아이템 수정 (수량/선택 상태 변경)
     * PATCH /api/users/cart/items/{cartItemId}
     */
    @PatchMapping("/cart/items/{cartItemId}")
    @ApiResponseFormat(message = "장바구니 아이템이 성공적으로 수정되었습니다.")
    public ApiResponse<Void> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        cartService.updateCartItem(cartItemId, request, userDetails);
        return ApiResponse.success(null);
    }
  
    /**
     * 장바구니 아이템 삭제
     * DELETE /api/users/cart/items/{cartItemId}
     */
    @DeleteMapping("/cart/items/{cartItemId}")
    @ApiResponseFormat(message = "장바구니 아이템이 성공적으로 삭제되었습니다.")
    public ApiResponse<Void> deleteCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        cartService.deleteCartItem(cartItemId, userDetails);
        return ApiResponse.success(null);
    }
}
