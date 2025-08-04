package com.cMall.feedShop.order.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.application.service.OrderService;
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
}
