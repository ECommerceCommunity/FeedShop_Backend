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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
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
}
