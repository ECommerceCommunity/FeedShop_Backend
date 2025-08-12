package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.RewardGrantRequest;
import com.cMall.feedShop.user.application.dto.response.RewardHistoryResponse;
import com.cMall.feedShop.user.application.dto.response.RewardPolicyResponse;
import com.cMall.feedShop.user.application.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    /**
     * 관리자 포인트 지급
     */
    @PostMapping("/admin/grant")
    public ApiResponse<RewardHistoryResponse> grantPointsByAdmin(
            @Valid @RequestBody RewardGrantRequest request,
            @AuthenticationPrincipal UserDetails adminDetails) {
        
        RewardHistoryResponse response = rewardService.grantPointsByAdmin(request, adminDetails);
        return ApiResponse.success(response);
    }

    /**
     * 리워드 히스토리 조회
     */
    @GetMapping("/history")
    public ApiResponse<Page<RewardHistoryResponse>> getRewardHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<RewardHistoryResponse> response = rewardService.getRewardHistory(userDetails, page, size);
        return ApiResponse.success(response);
    }

    /**
     * 리워드 정책 조회
     */
    @GetMapping("/policies")
    public ApiResponse<List<RewardPolicyResponse>> getRewardPolicies() {
        List<RewardPolicyResponse> response = rewardService.getRewardPolicies();
        return ApiResponse.success(response);
    }
}
