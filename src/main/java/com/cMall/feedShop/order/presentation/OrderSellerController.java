package com.cMall.feedShop.order.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.order.application.dto.request.OrderStatusUpdateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.application.dto.response.OrderStatusUpdateResponse;
import com.cMall.feedShop.order.application.service.OrderService;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문 (판매자)", description = "판매자 주문 관리 관련 API")
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Validated
public class OrderSellerController {

    private final OrderService orderService;

    @Operation(
            summary = "판매자 주문 목록 조회",
            description = "판매자가 자신의 가게에서 판매한 상품들의 주문 목록을 페이지네이션으로 조회합니다. 주문 상태별 필터링이 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "판매자 주문 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderPageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 페이지 파라미터 또는 주문 상태"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음"
            )
    })
    @GetMapping("/orders")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "판매자 주문 목록 조회 완료")
    public ApiResponse<OrderPageResponse> getOrderList(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
            int page,
            @Parameter(description = "페이지 크기 (0~1000)")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 1000, message = "페이지 크기는 1000 이하이어야 합니다.")
            int size,
            @Parameter(
                    description = "주문 상태 필터링(전체 조회시 'ALL' 또는 'NULL')",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ORDERED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED", "ALL"}
                    ))
            @RequestParam(required = false)
            String status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        OrderPageResponse data = orderService.getOrderListForSeller(page, size, status, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "판매자 주문 상태 변경",
            description = "판매자가 자신의 가게 상품 주문의 상태를 변경합니다. 주문 확인(ORDERED), 배송 시작(SHIPPED), 배송 완료(DELIVERED) 등의 상태로 변경할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주문 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = OrderStatusUpdateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (변경할 수 없는 상태)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음 또는 다른 가게의 주문에 접근 시도"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없음"
            )
    })
    @PostMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "주문 상태가 변경되었습니다.")
    public ApiResponse<OrderStatusUpdateResponse> updateOrderStatus(
            @Parameter(description = "상태를 변경할 주문 ID", required = true, example = "1")
            @PathVariable
            @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.")
            Long orderId,
            @Parameter(description = "주문 상태 변경 요청 (변경할 상태와 사유)", required = true)
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        OrderStatusUpdateResponse data = orderService.updateOrderStatus(orderId, request, currentUser.getUsername());
        return ApiResponse.success(data);
    }
}
