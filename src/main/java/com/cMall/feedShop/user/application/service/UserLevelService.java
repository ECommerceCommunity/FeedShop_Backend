package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.UserStatsResponse;
import com.cMall.feedShop.user.domain.model.*;
import com.cMall.feedShop.user.domain.repository.UserActivityRepository;
import com.cMall.feedShop.user.domain.repository.UserLevelRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserStatsRepository;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserLevelService {
    
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserLevelRepository userLevelRepository;
    private final BadgeService badgeService;
    
    /**
     * 사용자 활동 기록 및 점수 부여
     */
    @Transactional
    public void recordActivity(Long userId, ActivityType activityType, String description, 
                              Long referenceId, String referenceType) {
        try {
            User user = getUserById(userId);
            
            // 중복 방지 체크 (참조 ID가 있는 경우)
            if (referenceId != null && referenceType != null) {
                boolean exists = userActivityRepository.existsByUserAndReferenceIdAndReferenceType(
                    user, referenceId, referenceType);
                if (exists) {
                    log.debug("Activity already recorded: userId={}, referenceId={}, referenceType={}", 
                             userId, referenceId, referenceType);
                    return;
                }
            }
            
            // 활동 기록 저장
            UserActivity activity = UserActivity.builder()
                    .user(user)
                    .activityType(activityType)
                    .description(description)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .build();
            
            userActivityRepository.save(activity);
            
            // 사용자 통계 업데이트
            UserStats userStats = getOrCreateUserStats(user);
            java.util.List<UserLevel> allLevels = userLevelRepository.findAllOrderByMinPointsRequired();
            boolean levelUp = userStats.addPoints(activityType.getPoints(), allLevels);
            userStatsRepository.save(userStats);
            
            log.info("Activity recorded: userId={}, activityType={}, points={}, levelUp={}", 
                    userId, activityType, activityType.getPoints(), levelUp);
            
            // 레벨업 시 레벨 관련 뱃지 체크 및 보상 처리
            if (levelUp) {
                handleLevelUp(user, userStats);
            }
            
        } catch (UserException e) {
            log.error("Failed to record activity: userId={}, activityType={}", userId, activityType, e);
            throw e; // 사용자 관련 예외는 다시 던짐
        } catch (Exception e) {
            log.error("Failed to record activity: userId={}, activityType={}", userId, activityType, e);
            // 점수 시스템 오류가 다른 비즈니스 로직에 영향주지 않도록 예외를 던지지 않음
        }
    }

    public UserStatsResponse getUserStatsResponse(Long userId) {
        UserStats userStats = getUserStats(userId);
        Long userRank = getUserRank(userId);

        java.util.List<UserLevel> allLevels = userLevelRepository.findAllOrderByMinPointsRequired();

        Integer pointsToNextLevel = userStats.getPointsToNextLevel(allLevels);
        Double levelProgress = userStats.getLevelProgress(allLevels);

        return UserStatsResponse.from(userStats, userRank, pointsToNextLevel, levelProgress);
    }

    /**
     * 레벨업 처리
     */
    private void handleLevelUp(User user, UserStats userStats) {
        UserLevel newLevel = userStats.getCurrentLevel();
        log.info("User leveled up: userId={}, newLevel={}", user.getId(), newLevel);
        
        // 레벨업 관련 뱃지 수여 체크
        checkAndAwardLevelBadges(user, newLevel);
        
        // 레벨별 보상 처리 (포인트, 쿠폰 등)
        processLevelUpRewards(user, newLevel);
    }
    
    /**
     * 레벨업 관련 뱃지 수여
     */
    private void checkAndAwardLevelBadges(User user, UserLevel level) {
        try {
            // 특정 레벨 달성 시 뱃지 수여 (레벨 ID로 판단)
            if (level.getLevelId() == 2) {
                // 첫 레벨업 기념 뱃지 (기존 뱃지 활용)
                badgeService.awardBadge(user.getId(), BadgeType.EARLY_ADOPTER);
            } else if (level.getLevelId() == 5) {
                // VIP 등급 달성
                badgeService.awardBadge(user.getId(), BadgeType.VIP);
            } else if (level.getLevelId() == 7) {
                // SNS 연계 권한 부여
                badgeService.awardBadge(user.getId(), BadgeType.SNS_CONNECTOR);
            } else if (level.getLevelId() == 9) {
                // 인플루언서 자격 부여
                badgeService.awardBadge(user.getId(), BadgeType.INFLUENCER);
            } else if (level.getLevelId() == 10) {
                // 최고 레벨 달성
                badgeService.awardBadge(user.getId(), BadgeType.LOYAL_CUSTOMER);
            }
        } catch (Exception e) {
            log.error("Failed to award level badges: userId={}, level={}", user.getId(), level, e);
        }
    }
    
    /**
     * 레벨업 보상 처리
     */
    private void processLevelUpRewards(User user, UserLevel level) {
        // TODO: 실제 보상 시스템과 연동
        // - 포인트 지급
        // - 쿠폰 발급
        // - 우선권 부여 등
        log.info("Level up rewards processed: userId={}, level={}, reward={}", 
                user.getId(), level, level.getRewardDescription());
    }
    
    /**
     * 사용자 통계 조회 또는 생성
     */
    @Transactional
    public UserStats getOrCreateUserStats(User user) {
        return userStatsRepository.findByUser(user)
                .orElseGet(() -> {
                    // 기본 레벨 1을 가져와서 사용자 통계 생성
                    UserLevel defaultLevel = userLevelRepository.findByMinPointsRequired(0)
                            .orElseThrow(() -> new IllegalStateException("기본 레벨을 찾을 수 없습니다."));
                    UserStats newStats = UserStats.builder().user(user).currentLevel(defaultLevel).build();
                    return userStatsRepository.save(newStats);
                });
    }
    
    /**
     * 사용자 현재 레벨 및 점수 조회
     */
    public UserStats getUserStats(Long userId) {
        User user = getUserById(userId);
        return getOrCreateUserStats(user);
    }
    
    /**
     * 사용자 순위 조회
     */
    public Long getUserRank(Long userId) {
        UserStats userStats = getUserStats(userId);
        return userStatsRepository.getUserRankByPoints(userStats.getTotalPoints());
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
