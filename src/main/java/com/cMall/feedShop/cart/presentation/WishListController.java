package com.cMall.feedShop.cart.presentation;

import com.cMall.feedShop.cart.application.dto.request.WishListRequest;
import com.cMall.feedShop.cart.application.dto.response.WishListAddResponse;
import com.cMall.feedShop.cart.application.service.WishlistService;
import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.cMall.feedShop.cart.application.dto.response.WishListResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class WishListController {
    private final WishlistService wishlistService;

    @PostMapping("/wishlist")
    @ApiResponseFormat(message = "상품이 찜 목록에 추가되었습니다.")
    public ApiResponse<WishListAddResponse> addWishList(
            @Valid @RequestBody WishListRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        WishListAddResponse response = wishlistService.addWishList(request, currentUser.getUsername());
        return ApiResponse.success(response);
    }

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
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        WishListResponse data = wishlistService.getWishList(page, size, currentUser.getUsername());
        return ApiResponse.success(data);
    }
}
