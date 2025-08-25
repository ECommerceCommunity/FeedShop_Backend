package com.cMall.feedShop.order.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.order.application.dto.request.DirectOrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderStatusUpdateRequest;
import com.cMall.feedShop.order.application.dto.response.*;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.application.dto.response.OrderDetailResponse;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse;
import com.cMall.feedShop.order.application.service.DirectOrderService;
import com.cMall.feedShop.order.application.service.OrderService;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
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

@Tag(name = "주문 (사용자)", description = "사용자 주문 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class OrderUserController {

    private final OrderService orderService;
    private final DirectOrderService directOrderService;
    private final PurchasedItemService purchasedItemService;

    @Operation(
            summary = "장바구니 주문 생성",
            description = "장바구니에 있는 선택된 상품들로 주문을 생성합니다. 주문 완료 시 장바구니에서 해당 상품들이 자동 제거됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주문 생성 성공",
                    content = @Content(schema = @Schema(implementation = OrderCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (비어있는 장바구니, 잘못된 배송지 정보 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "재고 부족 또는 포인트 부족"
            )
    })
    @PostMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat(message = "주문이 성공적으로 생성되었습니다.")
    public ApiResponse<OrderCreateResponse> createOrder(
            @Parameter(description = "주문 생성 요청 (배송지, 결제 정보, 사용 포인트 등)", required = true)
            @Valid @RequestBody OrderCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = (User) userDetails;
        OrderCreateResponse data = orderService.createOrder(request, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "직접 주문 생성 (바로 구매)",
            description = "장바구니를 거치지 않고 상품 상세 페이지에서 바로 주문을 생성합니다. 특정 옵션과 수량을 지정하여 단일 상품을 주문할 때 사용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "직접 주문 생성 성공",
                    content = @Content(schema = @Schema(implementation = OrderCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (잘못된 옵션 ID, 수량 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품 또는 옵션을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "재고 부족"
            )
    })
    @PostMapping("/direct-orders")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat(message = "주문이 성공적으로 생성되었습니다.")
    public ApiResponse<OrderCreateResponse> createDirectOrder(
            @Parameter(description = "직접 주문 생성 요청 (상품 옵션, 수량, 배송지 정보 등)", required = true)
            @Valid @RequestBody DirectOrderCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        OrderCreateResponse data = directOrderService.createDirectOrder(request, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "주문 목록 조회",
            description = "현재 사용자의 주문 내역을 페이지네이션으로 조회합니다. 주문 상태로 필터링이 가능하며, 최신 주문 순으로 정렬됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주문 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderPageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 페이지 파라미터 또는 주문 상태"
            )
    })
    @GetMapping("/orders")
    @ApiResponseFormat(message = "주문 목록 조회 완료")
    public ApiResponse<OrderPageResponse> getOrderList(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
            int page,
            @Parameter(description = "페이지 크기 (1~100)")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "페이지 크기는 최대 100이어야 합니다.")
            int size,
            @Parameter(
                    description = "주문 상태 필터링(전체 조회시 'ALL' 또는 'NULL')",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ORDERED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED", "ALL"}
                    ))
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        OrderPageResponse data = orderService.getOrderListForUser(page, size, status, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "주문 상세 조회",
            description = "특정 주문의 상세 정보를 조회합니다. 주문한 상품 목록, 배송지 정보, 결제 정보, 현재 주문 상태 등을 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주문 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "다른 사용자의 주문에 접근 시도"
            )
    })
    @GetMapping("/orders/{orderId}")
    @ApiResponseFormat(message = "주문 상세 조회 완료")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @Parameter(description = "조회할 주문 ID", required = true, example = "1")
            @PathVariable
            @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.")
            Long orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        OrderDetailResponse data = orderService.getOrderDetail(orderId, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "구매한 상품 목록 조회",
            description = "피드 작성 시 사용할 수 있는 구매한 상품 목록을 조회합니다. 배송 완료된 주문의 상품만 조회됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "구매한 상품 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PurchasedItemListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/orders/items")
    @ApiResponseFormat(message = "구매한 상품 목록을 성공적으로 조회했습니다.")
    public ApiResponse<PurchasedItemListResponse> getPurchasedItems(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        PurchasedItemListResponse response = purchasedItemService.getPurchasedItems(currentUser.getUsername());
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "사용자 주문 상태 업데이트",
            description = "사용자가 자신의 주문 상태를 변경합니다. 주문 취소(CANCELLED) 또는 반품 요청(RETURNED) 상태로 변경할 수 있습니다."
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
                    responseCode = "404",
                    description = "주문을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "다른 사용자의 주문에 접근 시도"
            )
    })
    @PostMapping("/orders/{orderId}/status")
    @ApiResponseFormat(message = "주문 상태가 변경되었습니다.")
    public ApiResponse<OrderStatusUpdateResponse> updateUserOrderStatus(
            @Parameter(description = "상태를 변경할 주문 ID", required = true, example = "1")
            @PathVariable
            @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.")
            Long orderId,
            @Parameter(description = "주문 상태 변경 요청 (변경할 상태와 사유)", required = true)
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
        ) {
        User currentUser = (User) userDetails;
        OrderStatusUpdateResponse data = orderService.updateUserOrderStatus(orderId, request, currentUser.getUsername());
        return ApiResponse.success(data);
    }
}
