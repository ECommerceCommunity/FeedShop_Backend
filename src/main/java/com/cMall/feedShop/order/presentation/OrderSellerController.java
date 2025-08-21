package com.cMall.feedShop.order.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.order.application.dto.request.OrderStatusUpdateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.application.dto.response.OrderStatusUpdateResponse;
import com.cMall.feedShop.order.application.service.OrderService;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Validated
public class OrderSellerController {

    private final OrderService orderService;

    /**
     * 판매자 주문 목록 조회 API
     * GET /api/seller/orders
     * 판매자가 자신의 상품에 대한 주문 목록을 조회
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "판매자 주문 목록 조회 완료")
    public ApiResponse<OrderPageResponse> getOrderList(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
            int page,
            @Parameter(description = "페이지 크기 (0~100)")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "페이지 크기는 100 이하이어야 합니다.")
            int size,
            @Parameter(
                    description = "주문 상태 필터링(전체 조회시 'ALL' 또는 'NULL')",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ORDERED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED", "ALL"}
                    ))
            @RequestParam(required = false)
            String status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        OrderPageResponse data = orderService.getOrderListForSeller(page, size, status, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    /**
     * 판매자 주문 상태 변경 API
     * POST /api/seller/orders/{orderId}/status
     * 판매자가 자신의 상품 주문 상태를 변경
     */
    @PostMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "주문 상태가 변경되었습니다.")
    public ApiResponse<OrderStatusUpdateResponse> updateOrderStatus(
            @PathVariable
            @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.")
            Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        OrderStatusUpdateResponse data = orderService.updateOrderStatus(orderId, request, currentUser.getUsername());
        return ApiResponse.success(data);
    }
}
