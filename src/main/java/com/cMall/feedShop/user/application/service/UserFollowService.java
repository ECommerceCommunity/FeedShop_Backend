package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.response.FollowToggleResponseDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowCountResponseDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowItemDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowListResponseDto;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserFollow;
import com.cMall.feedShop.user.domain.repository.UserFollowRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 팔로우 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    /**
     * 팔로우/언팔로우 토글
     * - 팔로우 관계가 없으면 생성, 있으면 삭제
     * - 자기 자신을 팔로우할 수 없음
     * 
     * @param followerId 팔로워 ID (팔로우를 하는 사용자)
     * @param followingId 팔로잉 ID (팔로우를 받는 사용자)
     * @return 팔로우 상태 및 수 정보
     */
    @Transactional
    public FollowToggleResponseDto toggleFollow(Long followerId, Long followingId) {
        log.info("팔로우 토글 요청 - followerId: {}, followingId: {}", followerId, followingId);

        // 자기 자신을 팔로우할 수 없음
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자기 자신을 팔로우할 수 없습니다.");
        }

        // 사용자 존재 여부 확인
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "팔로워 사용자를 찾을 수 없습니다."));
        
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "팔로잉 사용자를 찾을 수 없습니다."));

        // 기존 팔로우 관계 확인
        boolean exists = userFollowRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId);
        boolean isFollowing;

        if (exists) {
            // 언팔로우
            userFollowRepository.deleteByFollower_IdAndFollowing_Id(followerId, followingId);
            isFollowing = false;
            log.info("언팔로우 완료 - followerId: {}, followingId: {}", followerId, followingId);
        } else {
            // 팔로우
            UserFollow userFollow = UserFollow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            
            // 팔로우 관계 검증
            userFollow.validateFollow();
            
            userFollowRepository.save(userFollow);
            isFollowing = true;
            log.info("팔로우 완료 - followerId: {}, followingId: {}", followerId, followingId);
        }

        // 팔로워/팔로잉 수 조회
        long followerCount = userFollowRepository.countByFollowing_Id(followingId);
        long followingCount = userFollowRepository.countByFollower_Id(followerId);

        String message = isFollowing ? "팔로우가 완료되었습니다." : "언팔로우가 완료되었습니다.";

        return FollowToggleResponseDto.builder()
                .following(isFollowing)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .message(message)
                .build();
    }

    /**
     * 사용자의 팔로워/팔로잉 수 조회
     * 
     * @param userId 사용자 ID
     * @return 팔로워/팔로잉 수 정보
     */
    @Transactional(readOnly = true)
    public UserFollowCountResponseDto getUserFollowCount(Long userId) {
        log.info("사용자 팔로우 수 조회 요청 - userId: {}", userId);

        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        long followerCount = userFollowRepository.countByFollowing_Id(userId);
        long followingCount = userFollowRepository.countByFollower_Id(userId);

        log.info("사용자 팔로우 수 조회 완료 - userId: {}, 팔로워: {}, 팔로잉: {}", userId, followerCount, followingCount);

        return UserFollowCountResponseDto.builder()
                .userId(userId)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    /**
     * 사용자의 팔로워 목록 조회 (페이징)
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 팔로워 목록
     */
    @Transactional(readOnly = true)
    public UserFollowListResponseDto getFollowers(Long userId, int page, int size) {
        log.info("사용자 팔로워 목록 조회 요청 - userId: {}, page: {}, size: {}", userId, page, size);

        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        // 페이징 및 정렬 설정
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 팔로워 목록 조회
        Page<UserFollow> followersPage = userFollowRepository.findFollowersByUserId(userId, pageable);
        
        // DTO 변환
        List<UserFollowItemDto> followers = followersPage.getContent().stream()
                .map(this::toUserFollowItemDto)
                .collect(Collectors.toList());

        // PaginatedResponse 생성
        PaginatedResponse<UserFollowItemDto> pagination = PaginatedResponse.<UserFollowItemDto>builder()
                .content(followers)
                .page(page)
                .size(size)
                .totalElements(followersPage.getTotalElements())
                .totalPages(followersPage.getTotalPages())
                .hasNext(followersPage.hasNext())
                .hasPrevious(followersPage.hasPrevious())
                .build();

        log.info("사용자 팔로워 목록 조회 완료 - userId: {}, 총 {}명", userId, followersPage.getTotalElements());

        return UserFollowListResponseDto.builder()
                .userId(userId)
                .users(followers)
                .pagination(pagination)
                .build();
    }

    /**
     * 사용자의 팔로잉 목록 조회 (페이징)
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 팔로잉 목록
     */
    @Transactional(readOnly = true)
    public UserFollowListResponseDto getFollowings(Long userId, int page, int size) {
        log.info("사용자 팔로잉 목록 조회 요청 - userId: {}, page: {}, size: {}", userId, page, size);

        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        // 페이징 및 정렬 설정
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 팔로잉 목록 조회
        Page<UserFollow> followingsPage = userFollowRepository.findFollowingsByUserId(userId, pageable);
        
        // DTO 변환
        List<UserFollowItemDto> followings = followingsPage.getContent().stream()
                .map(this::toUserFollowItemDto)
                .collect(Collectors.toList());

        // PaginatedResponse 생성
        PaginatedResponse<UserFollowItemDto> pagination = PaginatedResponse.<UserFollowItemDto>builder()
                .content(followings)
                .page(page)
                .size(size)
                .totalElements(followingsPage.getTotalElements())
                .totalPages(followingsPage.getTotalPages())
                .hasNext(followingsPage.hasNext())
                .hasPrevious(followingsPage.hasPrevious())
                .build();

        log.info("사용자 팔로잉 목록 조회 완료 - userId: {}, 총 {}명", userId, followingsPage.getTotalElements());

        return UserFollowListResponseDto.builder()
                .userId(userId)
                .users(followings)
                .pagination(pagination)
                .build();
    }

    /**
     * 사용자가 특정 사용자를 팔로우하고 있는지 확인
     * 
     * @param followerId 팔로워 ID
     * @param followingId 팔로잉 ID
     * @return 팔로우 여부
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return userFollowRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId);
    }

    /**
     * 사용자가 팔로우하고 있는 사용자 ID 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 팔로우하고 있는 사용자 ID 목록
     */
    @Transactional(readOnly = true)
    public List<Long> getFollowingUserIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userFollowRepository.findFollowingUserIdsByUserId(userId);
    }

    /**
     * 사용자를 팔로우하고 있는 사용자 ID 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 팔로우하고 있는 사용자 ID 목록
     */
    @Transactional(readOnly = true)
    public List<Long> getFollowerUserIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userFollowRepository.findFollowerUserIdsByUserId(userId);
    }

    /**
     * UserFollow 엔티티를 UserFollowItemDto로 변환
     */
    private UserFollowItemDto toUserFollowItemDto(UserFollow userFollow) {
        User user = userFollow.getFollower() != null ? userFollow.getFollower() : userFollow.getFollowing();
        
        String nickname = null;
        String profileImageUrl = null;
        
        if (user.getUserProfile() != null) {
            nickname = user.getUserProfile().getNickname();
            // TODO: UserProfile에 profileImageUrl 필드 추가 시 구현
            profileImageUrl = null;
        }

        return UserFollowItemDto.builder()
                .userId(user.getId())
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .followedAt(userFollow.getCreatedAt())
                .build();
    }
}
