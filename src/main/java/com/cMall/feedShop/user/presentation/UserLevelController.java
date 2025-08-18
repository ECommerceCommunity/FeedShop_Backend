package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.dto.UserActivityResponse;
import com.cMall.feedShop.user.application.dto.UserRankingResponse;
import com.cMall.feedShop.user.application.dto.UserStatsResponse;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserActivity;
import com.cMall.feedShop.user.domain.model.UserStats;
import com.cMall.feedShop.user.domain.repository.UserActivityRepository;
import com.cMall.feedShop.user.domain.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/level")
@RequiredArgsConstructor
public class UserLevelController {
    
    private final UserLevelService userLevelService;
    private final UserStatsRepository userStatsRepository;
    private final UserActivityRepository userActivityRepository;
    
    /**
     * 현재 사용자의 레벨 및 점수 정보 조회
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserStatsResponse> getMyStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        UserStats userStats = userLevelService.getUserStats(user.getId());
        Long userRank = userLevelService.getUserRank(user.getId());
        
        UserStatsResponse response = UserStatsResponse.from(userStats, userRank);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<UserStatsResponse> getUserStats(@PathVariable Long userId) {
        UserStats userStats = userLevelService.getUserStats(userId);
        Long userRank = userLevelService.getUserRank(userId);
        
        UserStatsResponse response = UserStatsResponse.from(userStats, userRank);
        return ResponseEntity.ok(response);
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
