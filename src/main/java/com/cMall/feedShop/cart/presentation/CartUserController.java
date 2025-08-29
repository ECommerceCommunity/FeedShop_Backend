package com.cMall.feedShop.cart.presentation;

import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.request.CartItemUpdateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.application.service.CartCreateService;
import com.cMall.feedShop.cart.application.service.CartDeleteService;
import com.cMall.feedShop.cart.application.service.CartReadService;
import com.cMall.feedShop.cart.application.service.CartUpdateService;
import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장바구니", description = "장바구니 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class CartUserController {

    private final CartCreateService cartCreateService;
    private final CartReadService cartReadService;
    private final CartUpdateService cartUpdateService;
    private final CartDeleteService cartDeleteService;

    @Operation(
            summary = "장바구니에 상품 추가",
            description = "선택한 상품 옵션과 이미지를 장바구니에 추가합니다. 동일한 옵션과 이미지가 이미 있다면 수량이 누적됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 추가 성공",
                    content = @Content(schema = @Schema(implementation = CartItemResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (옵션 ID, 이미지 ID, 수량 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품 옵션 또는 이미지를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "재고 부족"
            )
    })
    @PostMapping("/cart/items")
    @ApiResponseFormat(message = "상품이 장바구니에 추가되었습니다.")
    public ApiResponse<CartItemResponse> addCartItem(
            @Parameter(description = "장바구니 아이템 생성 요청", required = true)
            @Valid @RequestBody CartItemCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = (User) userDetails;
        CartItemResponse data = cartCreateService.addCartItem(request, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "장바구니 목록 조회",
            description = "현재 사용자의 장바구니에 담긴 모든 상품을 조회합니다. 상품 정보, 할인 가격, 총 금액 등을 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 조회 성공",
                    content = @Content(schema = @Schema(implementation = CartItemListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/cart")
    @ApiResponseFormat(message = "장바구니 목록을 성공적으로 조회했습니다.")
    public ApiResponse<CartItemListResponse> getCartItems(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        CartItemListResponse data = cartReadService.getCartItems(currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "장바구니 아이템 수정",
            description = "장바구니 아이템의 수량을 변경하거나 선택 상태를 토글합니다. 선택된 아이템만 주문 시 결제 대상이 됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 아이템 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (수량 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니 아이템을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "재고 부족"
            )
    })
    @PatchMapping("/cart/items/{cartItemId}")
    @ApiResponseFormat(message = "장바구니 아이템이 성공적으로 수정되었습니다.")
    public ApiResponse<Void> updateCartItem(
            @Parameter(description = "장바구니 아이템 ID", required = true, example = "1")
            @PathVariable Long cartItemId,
            @Parameter(description = "장바구니 아이템 수정 요청", required = true)
            @Valid @RequestBody CartItemUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        cartUpdateService.updateCartItem(cartItemId, request, currentUser.getUsername());
        return ApiResponse.success(null);
    }
  
    @Operation(
            summary = "장바구니 아이템 삭제",
            description = "장바구니에서 특정 상품 아이템을 완전히 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 아이템 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니 아이템을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "다른 사용자의 장바구니 아이템에 접근 시도"
            )
    })
    @DeleteMapping("/cart/items/{cartItemId}")
    @ApiResponseFormat(message = "장바구니 아이템이 성공적으로 삭제되었습니다.")
    public ApiResponse<Void> deleteCartItem(
            @Parameter(description = "삭제할 장바구니 아이템 ID", required = true, example = "1")
            @PathVariable Long cartItemId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        cartDeleteService.deleteCartItem(cartItemId, currentUser.getUsername());
        return ApiResponse.success(null);
    }
}
