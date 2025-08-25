package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.dto.BadgeAwardRequest;
import com.cMall.feedShop.user.application.dto.BadgeListResponse;
import com.cMall.feedShop.user.application.dto.BadgeResponse;
import com.cMall.feedShop.user.application.dto.BadgeToggleRequest;
import com.cMall.feedShop.user.application.service.BadgeService;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/badges")
@RequiredArgsConstructor
public class BadgeController {
    
    private final BadgeService badgeService;
    
    /**
     * 현재 사용자의 모든 뱃지 조회
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BadgeListResponse> getMyBadges(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        BadgeListResponse response = badgeService.getUserBadges(user.getId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 현재 사용자의 표시되는 뱃지만 조회
     */
    @GetMapping("/me/displayed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BadgeListResponse> getMyDisplayedBadges(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        BadgeListResponse response = badgeService.getUserDisplayedBadges(user.getId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 특정 사용자의 표시되는 뱃지 조회 (다른 사용자가 볼 수 있는 뱃지)
     */
    @GetMapping("/users/{userId}/displayed")
    public ResponseEntity<BadgeListResponse> getUserDisplayedBadges(@PathVariable Long userId) {
        BadgeListResponse response = badgeService.getUserDisplayedBadges(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 뱃지 표시/숨김 토글
     */
    @PatchMapping("/toggle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BadgeResponse> toggleBadgeDisplay(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody BadgeToggleRequest request) {
        User user = (User) userDetails;
        BadgeResponse response = badgeService.toggleBadgeDisplay(user.getId(), request.getBadgeId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 관리자용: 특정 사용자에게 뱃지 수여
     */
    @PostMapping("/admin/award")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BadgeResponse> awardBadge(@RequestBody BadgeAwardRequest request) {
        BadgeResponse response = badgeService.awardBadge(request.getUserId(), request.getBadgeType());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 관리자용: 특정 사용자의 모든 뱃지 조회
     */
    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BadgeListResponse> getUserBadgesForAdmin(@PathVariable Long userId) {
        BadgeListResponse response = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(response);
    }
}
