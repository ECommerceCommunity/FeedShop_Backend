package com.cMall.feedShop.order.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse;
import com.cMall.feedShop.order.application.service.OrderService;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class OrderUserController {

    private final OrderService orderService;
    private final PurchasedItemService purchasedItemService;

    /**
     * 주문 생성 API
     * POST /api/users/orders
     * USER 권한이 있는 로그인된 사용자만 주문 가능
     */
    @PostMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat(message = "주문이 성공적으로 생성되었습니다.")
    public ApiResponse<OrderCreateResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderCreateResponse data = orderService.createOrder(request, userDetails);
        return ApiResponse.success(data);
    }

    /**
     * 구매한 상품 목록 조회 API
     * 피드 작성 시 사용할 구매 상품
     * GET /api/users/orders/items
     * @param userDetails
     * @return
     */
    @GetMapping("/orders/items")
    @ApiResponseFormat(message = "구매한 상품 목록을 성공적으로 조회했습니다.")
    public ApiResponse<PurchasedItemListResponse> getPurchasedItems(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PurchasedItemListResponse response = purchasedItemService.getPurchasedItems(userDetails);
        return ApiResponse.success(response);
    }
}
