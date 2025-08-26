package com.cMall.feedShop.store.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.store.application.dto.response.StoreListResponse;
import com.cMall.feedShop.store.application.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "가게 (공개)", description = "공개 가게 조회 관련 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @Operation(
            summary = "가게 목록 조회",
            description = "전체 가게 목록을 조회합니다. 가게 이름, 설명, 연락처 등 기본 정보를 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "가게 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = StoreListResponse.class))
            )
    })
    @GetMapping
    @ApiResponseFormat(message = "가게 목록 조회 성공")
    public ApiResponse<List<StoreListResponse>> getAllStores() {
        List<StoreListResponse> storeList = storeService.getAllStores();
        return ApiResponse.success(storeList);
    }
}
