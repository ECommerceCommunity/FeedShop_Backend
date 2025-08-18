package com.cMall.feedShop.store.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.store.application.dto.response.StoreListResponse;
import com.cMall.feedShop.store.application.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    /**
     * 가게 목록 조회 API
     * @return ApiResponse<List<StoreListResponse>> - 가게 목록 응답
     */
    @GetMapping
    @ApiResponseFormat(message = "가게 목록 조회 성공")
    public ApiResponse<List<StoreListResponse>> getAllStores() {
        List<StoreListResponse> storeList = storeService.getAllStores();
        return ApiResponse.success(storeList);
    }
}
