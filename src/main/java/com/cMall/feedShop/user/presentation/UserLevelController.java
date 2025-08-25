package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.UserActivityResponse;
import com.cMall.feedShop.user.application.dto.UserRankingResponse;
import com.cMall.feedShop.user.application.dto.UserStatsResponse;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserActivity;
import com.cMall.feedShop.user.domain.model.UserStats;
import com.cMall.feedShop.user.domain.repository.UserActivityRepository;
import com.cMall.feedShop.user.domain.repository.UserLevelRepository;
import com.cMall.feedShop.user.domain.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.cMall.feedShop.common.exception.ErrorCode.FORBIDDEN;

@RestController
@RequestMapping("/api/users/level")
@RequiredArgsConstructor
public class UserLevelController {
    
    private final UserLevelService userLevelService;
    private final UserStatsRepository userStatsRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserLevelRepository userLevelRepository;

    /**
     * 현재 사용자의 레벨 및 점수 정보 조회
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat
    public ApiResponse<UserStatsResponse> getMyStats(@AuthenticationPrincipal UserDetails userDetails) {
        if (!(userDetails instanceof User)) {
            throw new UserException(FORBIDDEN, "인증된 사용자 정보를 찾을 수 없습니다.");
        }

        // 1. userDetails를 안전하게 User 객체로 캐스팅
        User user = (User) userDetails;

        // 2. userId를 사용해 서비스 계층의 통합된 메서드를 호출
        // 이 메서드가 모든 데이터 조회 및 계산을 담당합니다.
        UserStatsResponse response = userLevelService.getUserStatsResponse(user.getId());

        // 3. 서비스로부터 받은 DTO를 응답으로 반환
        return ApiResponse.success(response);
    }
    
    /**
     * 현재 사용자의 활동 내역 조회
     */
    @GetMapping("/me/activities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserActivityResponse>> getMyActivities(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = (User) userDetails;
        Pageable pageable = PageRequest.of(page, size);
        
        Page<UserActivity> activities = userActivityRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        Page<UserActivityResponse> response = activities.map(UserActivityResponse::from);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 특정 사용자의 공개 레벨 정보 조회
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<UserStatsResponse> getUserStats(@PathVariable Long userId) {
        UserStatsResponse response = userLevelService.getUserStatsResponse(userId);
        return ApiResponse.success(response);
    }
    
    /**
     * 점수 랭킹 조회 (상위 N명)
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<UserRankingResponse>> getRanking(
            @RequestParam(defaultValue = "50") int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<UserStats> topUsers = userStatsRepository.findTopUsersByPoints(pageable);
        
        List<UserRankingResponse> response = topUsers.getContent().stream()
                .map(userStats -> {
                    Long rank = userStatsRepository.getUserRankByPoints(userStats.getTotalPoints());
                    return UserRankingResponse.from(userStats, rank);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 관리자용: 특정 사용자에게 점수 부여
     */
    @PostMapping("/admin/award-points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> awardPoints(
            @RequestParam Long userId,
            @RequestParam ActivityType activityType,
            @RequestParam(required = false) String description) {
        userLevelService.recordActivity(userId, activityType, description, null, "ADMIN");
        return ResponseEntity.ok("점수가 성공적으로 부여되었습니다.");
    }
    
    /**
     * 관리자용: 사용자 활동 내역 조회
     */
    @GetMapping("/admin/activities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserActivityResponse>> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivity> activities = userActivityRepository.findRecentActivities(pageable);
        Page<UserActivityResponse> response = activities.map(UserActivityResponse::from);
        
        return ResponseEntity.ok(response);
    }
}
