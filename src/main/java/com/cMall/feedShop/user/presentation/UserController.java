package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.dto.request.UserWithdrawRequest;
import com.cMall.feedShop.user.application.dto.response.UserProfileResponse;
import com.cMall.feedShop.user.application.service.UserProfileService;
import com.cMall.feedShop.user.application.service.UserService;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;
    private final UserService userService;

    // 사용자 프로필을 조회하는 예시 메서드
    @GetMapping("/{userId}/profile")
    public UserProfileResponse getUserProfile(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        // 현재 로그인한 사용자의 ID 가져오기
        User currentUser = (User) userDetails;
        Long currentUserId = currentUser.getId();

        // 요청된 userId와 현재 로그인한 사용자의 ID가 다른 경우 권한 확인
        if (!currentUserId.equals(userId)) {
            // 관리자 권한이 있는지 확인 (예: ROLE_ADMIN)
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new AccessDeniedException("You do not have permission to view this profile.");
            }
        }

        // userProfileService를 사용하여 실제 비즈니스 로직 호출
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return response;
    }

    // 관리자가 이메일로 사용자 탈퇴 처리
    // (관리자 권한 필요)
    @DeleteMapping("/admin/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminWithdrawUserByEmail(@PathVariable String email) {
        userService.adminWithdrawUserByEmail(email); // 새로운 Service 메서드 호출
        return ResponseEntity.ok("사용자 탈퇴 처리 완료 (관리자)");
    }

    // 사용자가 자신의 계정을 이메일과 비밀번호로 탈퇴
    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> withdrawUser(@RequestBody UserWithdrawRequest request) {
        userService.withdrawCurrentUserWithPassword(request.getEmail(), request.getPassword());
        return ResponseEntity.ok("회원 탈퇴 처리 완료");
    }
}