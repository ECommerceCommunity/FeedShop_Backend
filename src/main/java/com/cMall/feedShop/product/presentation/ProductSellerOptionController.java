package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.product.application.dto.request.ProductOptionCreateRequest;
import com.cMall.feedShop.product.application.dto.request.ProductOptionUpdateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductOptionCreateResponse;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.application.service.*;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Tag(name = "상품 옵션 (판매자)", description = "판매자가 상품 옵션을 관리하는 API")
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class ProductSellerOptionController {
    
    private final ProductOptionReadService productOptionReadService;
    private final ProductOptionCreateService productOptionCreateService;
    private final ProductOptionUpdateService productOptionUpdateService;
    private final ProductOptionDeleteService productOptionDeleteService;

    @Operation(
            summary = "상품 옵션 목록 조회",
            description = "특정 상품에 대한 모든 옵션(색상, 사이즈, 재고, 가격 등) 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 옵션 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductOptionInfo.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음 또는 다른 판매자의 상품에 접근 시도"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음"
            )
    })
    @GetMapping("/products/{productId}/options")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품 옵션 정보를 조회했습니다.")
    public ApiResponse<List<ProductOptionInfo>> getProductOptions(
            @Parameter(description = "옵션을 조회할 상품 ID", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;

        // 상품 ID로 상품 옵션 정보를 조회
        List<ProductOptionInfo> options = productOptionReadService.getProductOptions(productId, currentUser.getLoginId());

        // 조회된 옵션 정보를 ApiResponse로 감싸서 반환
        return ApiResponse.success(options);
    }

    @Operation(
            summary = "상품 옵션 추가",
            description = "기존 상품에 새로운 옵션을 추가합니다. 색상, 사이즈, 재고 수량, 가격 등을 설정할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 옵션 추가 성공",
                    content = @Content(schema = @Schema(implementation = ProductOptionCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류, 중복 옵션)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음 또는 다른 판매자의 상품에 접근 시도"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음"
            )
    })
    @PostMapping("/products/{productId}/options")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품 옵션이 성공적으로 추가되었습니다.")
    public ApiResponse<ProductOptionCreateResponse> addProductOption(
            @Parameter(description = "옵션을 추가할 상품 ID", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(description = "상품 옵션 생성 요청 (색상, 사이즈, 재고, 가격 등)", required = true)
            @Valid @RequestBody ProductOptionCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;

        ProductOptionCreateResponse option = productOptionCreateService.addProductOption(productId, request, currentUser.getLoginId());
        return ApiResponse.success(option);
    }

    @Operation(
            summary = "상품 옵션 수정",
            description = "기존 상품 옵션의 정보를 수정합니다. 색상, 사이즈, 재고 수량, 가격 등을 변경할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 옵션 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음 또는 다른 판매자의 옵션에 접근 시도"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품 옵션을 찾을 수 없음"
            )
    })
    @PutMapping("/products/options/{optionId}")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품 옵션이 성공적으로 수정되었습니다.")
    public ApiResponse<Void> updateProductOption(
            @Parameter(description = "수정할 상품 옵션 ID", required = true, example = "1")
            @PathVariable Long optionId,
            @Parameter(description = "상품 옵션 수정 요청 (색상, 사이즈, 재고, 가격 등)", required = true)
            @Valid @RequestBody ProductOptionUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;

        productOptionUpdateService.updateProductOption(optionId, request, currentUser.getLoginId());
        return ApiResponse.success(null);
    }

    @Operation(
            summary = "상품 옵션 삭제",
            description = "기존 상품 옵션을 삭제합니다. 주문과 연결된 옵션은 삭제할 수 없습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 옵션 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음 또는 다른 판매자의 옵션에 접근 시도"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품 옵션을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "주문과 연결된 옵션으로 삭제 불가"
            )
    })
    @DeleteMapping("/products/options/{optionId}")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품 옵션이 성공적으로 삭제되었습니다.")
    public ApiResponse<Void> deleteProductOption(
            @Parameter(description = "삭제할 상품 옵션 ID", required = true, example = "1")
            @PathVariable Long optionId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;

        productOptionDeleteService.deleteProductOption(optionId, currentUser.getLoginId());
        return ApiResponse.success(null);
    }
}
