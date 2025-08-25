package com.cMall.feedShop.store.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.store.application.dto.response.StoreDetailResponse;
import com.cMall.feedShop.store.application.service.StoreService;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가게 (판매자)", description = "판매자 가게 관리 관련 API")
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class StoreSellerController {
    private final StoreService storeService;

    @Operation(
            summary = "내 가게 상세 정보 조회",
            description = "판매자가 자신의 가게 상세 정보를 조회합니다. 가게 이름, 설명, 연락처, 주소 등의 정보를 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 가게 상세 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = StoreDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "가게 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/stores")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "판매자 가게 목록을 성공적으로 조회했습니다.")
    public ApiResponse<StoreDetailResponse> getMyStore(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        StoreDetailResponse data = storeService.getMyStoreDetail(currentUser.getId());
        return ApiResponse.success(data);
    }
}
