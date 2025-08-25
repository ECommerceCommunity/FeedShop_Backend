package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.BadgeListResponse;
import com.cMall.feedShop.user.application.dto.BadgeResponse;
import com.cMall.feedShop.user.domain.model.BadgeType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserBadge;
import com.cMall.feedShop.user.domain.repository.UserBadgeRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BadgeService {
    
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    
    /**
     * 사용자의 모든 뱃지 조회
     */
    public BadgeListResponse getUserBadges(Long userId) {
        User user = getUserById(userId);
        List<UserBadge> userBadges = userBadgeRepository.findByUserOrderByAwardedAtDesc(user);
        
        List<BadgeResponse> badgeResponses = userBadges.stream()
                .map(BadgeResponse::from)
                .collect(Collectors.toList());
        
        long totalCount = userBadgeRepository.countByUser(user);
        long displayedCount = userBadgeRepository.countByUserAndIsDisplayedTrue(user);
        
        return BadgeListResponse.of(badgeResponses, totalCount, displayedCount);
    }
    
    /**
     * 사용자의 표시되는 뱃지만 조회
     */
    public BadgeListResponse getUserDisplayedBadges(Long userId) {
        User user = getUserById(userId);
        List<UserBadge> userBadges = userBadgeRepository.findByUserAndIsDisplayedTrueOrderByAwardedAtDesc(user);
        
        List<BadgeResponse> badgeResponses = userBadges.stream()
                .map(BadgeResponse::from)
                .collect(Collectors.toList());
        
        long totalCount = userBadgeRepository.countByUser(user);
        long displayedCount = userBadgeRepository.countByUserAndIsDisplayedTrue(user);
        
        return BadgeListResponse.of(badgeResponses, totalCount, displayedCount);
    }
    
    /**
     * 뱃지 수여
     */
    @Transactional
    public BadgeResponse awardBadge(Long userId, BadgeType badgeType) {
        User user = getUserById(userId);
        
        // 이미 해당 뱃지를 가지고 있는지 확인
        if (userBadgeRepository.existsByUserAndBadgeType(user, badgeType)) {
            log.warn("User {} already has badge {}", userId, badgeType);
            throw new IllegalArgumentException("이미 보유한 뱃지입니다.");
        }
        
        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badgeType(badgeType)
                .badgeName(badgeType.getName())
                .awardedAt(LocalDateTime.now())
                .isDisplayed(true)
                .build();
        
        UserBadge savedBadge = userBadgeRepository.save(userBadge);
        log.info("Badge {} awarded to user {}", badgeType, userId);
        
        // 뱃지 획득 시 보너스 점수 부여를 위한 활동 기록
        // 순환 참조를 피하기 위해 직접 호출하지 않고 이벤트나 별도 처리 필요
        recordBadgeAchievement(user, badgeType);
        
        return BadgeResponse.from(savedBadge);
    }
    
    /**
     * 뱃지 표시/숨김 토글
     */
    @Transactional
    public BadgeResponse toggleBadgeDisplay(Long userId, Long badgeId) {
        UserBadge userBadge = userBadgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("뱃지를 찾을 수 없습니다."));
        
        // 해당 뱃지가 사용자의 것인지 확인
        if (!userBadge.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        
        userBadge.toggleDisplay();
        log.info("Badge {} display toggled for user {}", badgeId, userId);
        
        return BadgeResponse.from(userBadge);
    }
    
    /**
     * 뱃지 자동 수여 체크 (구매 관련)
     */
    @Transactional
    public void checkAndAwardPurchaseBadges(Long userId, Long totalPurchaseCount, Long totalPurchaseAmount) {
        User user = getUserById(userId);
        
        // 첫 구매 뱃지
        if (totalPurchaseCount == 1) {
            awardBadgeIfNotExists(user, BadgeType.FIRST_PURCHASE);
        }
        
        // 구매 횟수 뱃지
        checkPurchaseCountBadges(user, totalPurchaseCount);
        
        // 구매 금액 뱃지
        checkPurchaseAmountBadges(user, totalPurchaseAmount);
    }
    
    /**
     * 뱃지 자동 수여 체크 (리뷰 관련)
     */
    @Transactional
    public void checkAndAwardReviewBadges(Long userId, Long totalReviewCount) {
        User user = getUserById(userId);
        
        // 첫 리뷰 뱃지
        if (totalReviewCount == 1) {
            awardBadgeIfNotExists(user, BadgeType.FIRST_REVIEW);
        }
        
        // 리뷰 개수 뱃지
        checkReviewCountBadges(user, totalReviewCount);
    }
    
    private void checkPurchaseCountBadges(User user, Long count) {
        if (count >= 100) {
            awardBadgeIfNotExists(user, BadgeType.PURCHASE_100);
        } else if (count >= 50) {
            awardBadgeIfNotExists(user, BadgeType.PURCHASE_50);
        } else if (count >= 20) {
            awardBadgeIfNotExists(user, BadgeType.PURCHASE_20);
        } else if (count >= 10) {
            awardBadgeIfNotExists(user, BadgeType.PURCHASE_10);
        } else if (count >= 5) {
            awardBadgeIfNotExists(user, BadgeType.PURCHASE_5);
        }
    }
    
    private void checkPurchaseAmountBadges(User user, Long amount) {
        if (amount >= 5_000_000) {
            awardBadgeIfNotExists(user, BadgeType.AMOUNT_5M);
        } else if (amount >= 1_000_000) {
            awardBadgeIfNotExists(user, BadgeType.AMOUNT_1M);
        } else if (amount >= 500_000) {
            awardBadgeIfNotExists(user, BadgeType.AMOUNT_500K);
        } else if (amount >= 100_000) {
            awardBadgeIfNotExists(user, BadgeType.AMOUNT_100K);
        }
    }
    
    private void checkReviewCountBadges(User user, Long count) {
        if (count >= 100) {
            awardBadgeIfNotExists(user, BadgeType.REVIEW_100);
        } else if (count >= 50) {
            awardBadgeIfNotExists(user, BadgeType.REVIEW_50);
        } else if (count >= 10) {
            awardBadgeIfNotExists(user, BadgeType.REVIEW_10);
        }
    }
    
    private void awardBadgeIfNotExists(User user, BadgeType badgeType) {
        if (!userBadgeRepository.existsByUserAndBadgeType(user, badgeType)) {
            UserBadge userBadge = UserBadge.builder()
                    .user(user)
                    .badgeType(badgeType)
                    .badgeName(badgeType.getName())
                    .awardedAt(LocalDateTime.now())
                    .isDisplayed(true)
                    .build();
            
            userBadgeRepository.save(userBadge);
            log.info("Auto-awarded badge {} to user {}", badgeType, user.getId());
        }
    }
    
    /**
     * 뱃지 획득 시 보너스 점수 기록
     */
    private void recordBadgeAchievement(User user, BadgeType badgeType) {
        // 추후 UserLevelService와 연동할 예정
        // 현재는 로그만 기록
        log.info("Badge achievement recorded: userId={}, badgeType={}, bonusPoints={}", 
                user.getId(), badgeType, badgeType.getBonusPoints());
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
