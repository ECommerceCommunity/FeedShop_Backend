package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.FollowToggleRequestDto;
import com.cMall.feedShop.user.application.dto.response.FollowToggleResponseDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowCountResponseDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowListResponseDto;
import com.cMall.feedShop.user.application.service.UserFollowService;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 사용자 팔로우 관련 API를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final UserRepository userRepository;

    /**
     * 팔로우/언팔로우 토글 API
     * POST /api/users/follow
     * 
     * @param requestDto 팔로우 요청 정보
     * @param userDetails 인증된 사용자 정보
     * @return 팔로우 상태 및 수 정보
     */
    @PostMapping("/follow")
    @ApiResponseFormat(message = "팔로우 상태가 변경되었습니다.", status = 200)
    public ResponseEntity<ApiResponse<FollowToggleResponseDto>> toggleFollow(
            @Valid @RequestBody FollowToggleRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("팔로우 토글 요청 - followingUserId: {}", requestDto.getFollowingUserId());
        
        // 현재 로그인한 사용자 ID 추출
        Long followerId = getUserIdFromUserDetails(userDetails);
        if (followerId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }
        
        // 팔로우/언팔로우 실행
        FollowToggleResponseDto result = userFollowService.toggleFollow(followerId, requestDto.getFollowingUserId());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 특정 사용자의 팔로워/팔로잉 수 조회 API
     * GET /api/users/{userId}/follow-count
     * 
     * @param userId 조회할 사용자 ID
     * @return 팔로워/팔로잉 수 정보
     */
    @GetMapping("/{userId}/follow-count")
    @ApiResponseFormat(message = "사용자 팔로우 수 정보입니다.", status = 200)
    public ResponseEntity<ApiResponse<UserFollowCountResponseDto>> getUserFollowCount(@PathVariable Long userId) {
        log.info("사용자 팔로우 수 조회 요청 - userId: {}", userId);
        
        UserFollowCountResponseDto result = userFollowService.getUserFollowCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 특정 사용자의 팔로워 목록 조회 API
     * GET /api/users/{userId}/followers
     * 
     * @param userId 조회할 사용자 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 팔로워 목록
     */
    @GetMapping("/{userId}/followers")
    @ApiResponseFormat(message = "사용자 팔로워 목록입니다.", status = 200)
    public ResponseEntity<ApiResponse<UserFollowListResponseDto>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("사용자 팔로워 목록 조회 요청 - userId: {}, page: {}, size: {}", userId, page, size);
        
        UserFollowListResponseDto result = userFollowService.getFollowers(userId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 특정 사용자의 팔로잉 목록 조회 API
     * GET /api/users/{userId}/followings
     * 
     * @param userId 조회할 사용자 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 팔로잉 목록
     */
    @GetMapping("/{userId}/followings")
    @ApiResponseFormat(message = "사용자 팔로잉 목록입니다.", status = 200)
    public ResponseEntity<ApiResponse<UserFollowListResponseDto>> getFollowings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("사용자 팔로잉 목록 조회 요청 - userId: {}, page: {}, size: {}", userId, page, size);
        
        UserFollowListResponseDto result = userFollowService.getFollowings(userId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 현재 사용자가 특정 사용자를 팔로우하고 있는지 확인하는 API
     * GET /api/users/{userId}/follow-status
     * 
     * @param userId 확인할 사용자 ID
     * @param userDetails 인증된 사용자 정보
     * @return 팔로우 상태
     */
    @GetMapping("/{userId}/follow-status")
    @ApiResponseFormat(message = "팔로우 상태 정보입니다.", status = 200)
    public ResponseEntity<ApiResponse<Boolean>> getFollowStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("팔로우 상태 확인 요청 - userId: {}", userId);
        
        // 현재 로그인한 사용자 ID 추출
        Long followerId = getUserIdFromUserDetails(userDetails);
        if (followerId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }
        
        // 팔로우 상태 확인
        boolean isFollowing = userFollowService.isFollowing(followerId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(isFollowing));
    }

    /**
     * 현재 사용자가 팔로우하고 있는 사용자 ID 목록 조회 API
     * GET /api/users/me/following-ids
     * 
     * @param userDetails 인증된 사용자 정보
     * @return 팔로우하고 있는 사용자 ID 목록
     */
    @GetMapping("/me/following-ids")
    @ApiResponseFormat(message = "내가 팔로우하고 있는 사용자 ID 목록입니다.", status = 200)
    public ResponseEntity<ApiResponse<java.util.List<Long>>> getMyFollowingIds(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("내 팔로잉 사용자 ID 목록 조회 요청");
        
        // 현재 로그인한 사용자 ID 추출
        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }
        
        // 팔로잉 사용자 ID 목록 조회
        java.util.List<Long> followingIds = userFollowService.getFollowingUserIds(userId);
        
        return ResponseEntity.ok(ApiResponse.success(followingIds));
    }

    /**
     * UserDetails에서 사용자 ID를 추출하는 헬퍼 메서드
     *
     * @param userDetails JWT 토큰에서 추출된 사용자 정보
     * @return 사용자 ID
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("UserDetails가 null입니다.");
            return null;
        }

        String username = userDetails.getUsername(); // JWT 토큰의 subject
        log.info("UserDetails에서 추출한 username: {}", username);

        // 1. 먼저 email로 시도
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.info("email로 사용자 찾음 - ID: {} (email: {})", user.getId(), username);
            return user.getId();
        }

        // 2. email로 찾지 못하면 loginId로 시도
        userOptional = userRepository.findByLoginId(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.info("loginId로 사용자 찾음 - ID: {} (loginId: {})", user.getId(), username);
            return user.getId();
        }

        log.warn("username '{}'로 사용자를 찾을 수 없습니다 (email, loginId 모두 시도)", username);
        return null;
    }
}
