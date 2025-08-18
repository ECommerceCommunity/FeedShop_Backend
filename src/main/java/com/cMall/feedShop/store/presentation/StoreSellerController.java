package com.cMall.feedShop.store.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.store.application.dto.response.StoreDetailResponse;
import com.cMall.feedShop.store.application.service.StoreService;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class StoreSellerController {
    private final StoreService storeService;

    @GetMapping("/stores")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "판매자 가게 목록을 성공적으로 조회했습니다.")
    public ApiResponse<StoreDetailResponse> getMyStore(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        StoreDetailResponse data = storeService.getMyStoreDetail(currentUser.getId());
        return ApiResponse.success(data);
    }
}
