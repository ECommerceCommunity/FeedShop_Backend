package com.cMall.feedShop.cart.presentation;

import com.cMall.feedShop.cart.application.dto.request.WishListRequest;
import com.cMall.feedShop.cart.application.dto.response.WishListCreateResponse;
import com.cMall.feedShop.cart.application.service.WishlistCreateService;
import com.cMall.feedShop.cart.application.service.WishlistDeleteService;
import com.cMall.feedShop.cart.application.service.WishlistReadService;
import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.cMall.feedShop.cart.application.dto.response.WishListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.annotation.Validated;

@Tag(name = "찜 목록", description = "찜 목록 (찜한 상품) 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class WishListController {
    private final WishlistCreateService wishlistCreateService;
    private final WishlistReadService wishlistReadService;
    private final WishlistDeleteService wishlistDeleteService;

    @Operation(
            summary = "상품 찜하기",
            description = "상품을 찜 목록에 추가합니다. 이미 찜한 상품인 경우 중복 추가되지 않습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "찜하기 성공",
                    content = @Content(schema = @Schema(implementation = WishListCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 찜한 상품"
            )
    })
    @PostMapping("/wishlist")
    @ApiResponseFormat(message = "상품이 찜 목록에 추가되었습니다.")
    public ApiResponse<WishListCreateResponse> addWishList(
            @Parameter(description = "찜할 상품 정보", required = true)
            @Valid @RequestBody WishListRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        WishListCreateResponse response = wishlistCreateService.addWishList(request, currentUser.getUsername());
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "찜한 상품 목록 조회",
            description = "현재 사용자가 찜한 모든 상품을 페이지네이션으로 조회합니다. 상품 기본 정보와 현재 가격 정보를 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "찜 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = WishListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 페이지 파라미터"
            )
    })
    @GetMapping("/wishlist")
    @ApiResponseFormat(message = "찜한 상품 목록을 성공적으로 조회했습니다.")
    public ApiResponse<WishListResponse> getWishList(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
            int page,
            @Parameter(description = "페이지 크기 (최대 100개)")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 최대 100개여야 합니다")
            int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        WishListResponse data = wishlistReadService.getWishList(page, size, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "찜한 상품 취소",
            description = "찜 목록에서 특정 상품을 제거합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "찜하기 취소 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "찜한 상품을 찾을 수 없음"
            )
    })
    @DeleteMapping("/wishlist/{productId}")
    @ApiResponseFormat(message = "찜한 상품이 성공적으로 취소되었습니다.")
    public ApiResponse<Void> deleteWishList(
            @Parameter(description = "취소할 상품 ID", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        wishlistDeleteService.deleteWishList(productId, currentUser.getUsername());
        return ApiResponse.success(null);
    }
}
