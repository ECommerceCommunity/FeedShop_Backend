package com.cMall.feedShop.order.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.application.service.OrderService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
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
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (0~100)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(
                    description = "주문 상태 필터링(전체 조회시 'ALL' 또는 'NULL')",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ORDERED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED", "ALL"}
                    ))
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        OrderPageResponse data = orderService.getOrderList(page, size, status, userDetails, true);
        return ApiResponse.success(data);
    }
}
