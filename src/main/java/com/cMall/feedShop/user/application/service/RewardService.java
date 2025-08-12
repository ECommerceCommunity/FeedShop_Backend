package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.request.RewardGrantRequest;
import com.cMall.feedShop.user.application.dto.response.RewardHistoryResponse;
import com.cMall.feedShop.user.application.dto.response.RewardPolicyResponse;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.RewardHistory;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.RewardHistoryRepository;
import com.cMall.feedShop.user.domain.repository.RewardPolicyRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardService {

    private final UserRepository userRepository;
    private final RewardPolicyRepository rewardPolicyRepository;
    private final RewardHistoryRepository rewardHistoryRepository;
    private final PointService pointService;

    /**
     * 관리자 포인트 지급
     */
    @Transactional
    public RewardHistoryResponse grantPointsByAdmin(RewardGrantRequest request, UserDetails adminDetails) {
        // 관리자 권한 검증
        User admin = validateAdmin(adminDetails);
        
        // 대상 사용자 조회
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        
        // 포인트 금액 검증
        if (request.getPoints() == null || request.getPoints() <= 0) {
            throw new UserException(ErrorCode.INVALID_POINT_AMOUNT);
        }
        
        // 리워드 히스토리 생성
        RewardHistory rewardHistory = RewardHistory.builder()
                .user(targetUser)
                .rewardType(RewardType.ADMIN_GRANT)
                .points(request.getPoints())
                .description(request.getDescription())
                .adminId(admin.getId())
                .build();
        
        RewardHistory savedHistory = rewardHistoryRepository.save(rewardHistory);
        
        // 포인트 적립 처리
        pointService.earnPoints(targetUser, request.getPoints(), 
                "관리자 지급: " + request.getDescription(), null);
        
        // 처리 완료 표시
        savedHistory.markAsProcessed();
        rewardHistoryRepository.save(savedHistory);
        
        log.info("관리자 포인트 지급 완료: 관리자 ID={}, 사용자 ID={}, 포인트={}, 사유={}", 
                admin.getId(), targetUser.getId(), request.getPoints(), request.getDescription());
        
        return RewardHistoryResponse.from(savedHistory);
    }

    /**
     * 리뷰 작성 보상 지급
     */
    @Transactional
    public RewardHistoryResponse grantReviewReward(User user, Long reviewId, String reviewType) {
        RewardType rewardType = getReviewRewardType(reviewType);
        RewardPolicy policy = getValidPolicy(rewardType);
        
        // 중복 지급 방지
        if (isAlreadyRewarded(reviewId, "REVIEW", rewardType)) {
            log.info("리뷰 보상 이미 지급됨: 사용자 ID={}, 리뷰 ID={}, 타입={}", user.getId(), reviewId, rewardType);
            return null;
        }
        
        // 리워드 히스토리 생성
        RewardHistory rewardHistory = RewardHistory.builder()
                .user(user)
                .rewardType(rewardType)
                .points(policy.getPoints())
                .description(policy.getDescription())
                .relatedId(reviewId)
                .relatedType("REVIEW")
                .build();
        
        RewardHistory savedHistory = rewardHistoryRepository.save(rewardHistory);
        
        // 포인트 적립 처리
        pointService.earnPoints(user, policy.getPoints(), 
                "리뷰 보상: " + policy.getDescription(), null);
        
        // 처리 완료 표시
        savedHistory.markAsProcessed();
        rewardHistoryRepository.save(savedHistory);
        
        log.info("리뷰 보상 지급 완료: 사용자 ID={}, 리뷰 ID={}, 포인트={}, 타입={}", 
                user.getId(), reviewId, policy.getPoints(), rewardType);
        
        return RewardHistoryResponse.from(savedHistory);
    }

    /**
     * 이벤트 참여 보상 지급
     */
    @Transactional
    public RewardHistoryResponse grantEventReward(User user, Long eventId, RewardType eventRewardType) {
        RewardPolicy policy = getValidPolicy(eventRewardType);
        
        // 중복 지급 방지
        if (isAlreadyRewarded(eventId, "EVENT", eventRewardType)) {
            log.info("이벤트 보상 이미 지급됨: 사용자 ID={}, 이벤트 ID={}, 타입={}", user.getId(), eventId, eventRewardType);
            return null;
        }
        
        // 일일/월간 제한 확인
        validateRewardLimits(user, eventRewardType);
        
        // 리워드 히스토리 생성
        RewardHistory rewardHistory = RewardHistory.builder()
                .user(user)
                .rewardType(eventRewardType)
                .points(policy.getPoints())
                .description(policy.getDescription())
                .relatedId(eventId)
                .relatedType("EVENT")
                .build();
        
        RewardHistory savedHistory = rewardHistoryRepository.save(rewardHistory);
        
        // 포인트 적립 처리
        pointService.earnPoints(user, policy.getPoints(), 
                "이벤트 보상: " + policy.getDescription(), null);
        
        // 처리 완료 표시
        savedHistory.markAsProcessed();
        rewardHistoryRepository.save(savedHistory);
        
        log.info("이벤트 보상 지급 완료: 사용자 ID={}, 이벤트 ID={}, 포인트={}, 타입={}", 
                user.getId(), eventId, policy.getPoints(), eventRewardType);
        
        return RewardHistoryResponse.from(savedHistory);
    }

    /**
     * 생일 축하 포인트 지급
     */
    @Transactional
    public RewardHistoryResponse grantBirthdayReward(User user) {
        RewardPolicy policy = getValidPolicy(RewardType.BIRTHDAY);
        
        // 올해 생일 보상 이미 지급되었는지 확인
        LocalDateTime now = LocalDateTime.now();
        if (hasReceivedBirthdayRewardThisYear(user, now.getYear())) {
            log.info("올해 생일 보상 이미 지급됨: 사용자 ID={}, 연도={}", user.getId(), now.getYear());
            return null;
        }
        
        // 리워드 히스토리 생성
        RewardHistory rewardHistory = RewardHistory.builder()
                .user(user)
                .rewardType(RewardType.BIRTHDAY)
                .points(policy.getPoints())
                .description(policy.getDescription())
                .build();
        
        RewardHistory savedHistory = rewardHistoryRepository.save(rewardHistory);
        
        // 포인트 적립 처리
        pointService.earnPoints(user, policy.getPoints(), 
                "생일 축하 포인트", null);
        
        // 처리 완료 표시
        savedHistory.markAsProcessed();
        rewardHistoryRepository.save(savedHistory);
        
        log.info("생일 축하 포인트 지급 완료: 사용자 ID={}, 포인트={}", user.getId(), policy.getPoints());
        
        return RewardHistoryResponse.from(savedHistory);
    }

    /**
     * 첫 구매 보너스 지급
     */
    @Transactional
    public RewardHistoryResponse grantFirstPurchaseReward(User user, Long orderId) {
        RewardPolicy policy = getValidPolicy(RewardType.FIRST_PURCHASE);
        
        // 첫 구매 보상 이미 지급되었는지 확인
        if (hasReceivedFirstPurchaseReward(user)) {
            log.info("첫 구매 보상 이미 지급됨: 사용자 ID={}", user.getId());
            return null;
        }
        
        // 리워드 히스토리 생성
        RewardHistory rewardHistory = RewardHistory.builder()
                .user(user)
                .rewardType(RewardType.FIRST_PURCHASE)
                .points(policy.getPoints())
                .description(policy.getDescription())
                .relatedId(orderId)
                .relatedType("ORDER")
                .build();
        
        RewardHistory savedHistory = rewardHistoryRepository.save(rewardHistory);
        
        // 포인트 적립 처리
        pointService.earnPoints(user, policy.getPoints(), 
                "첫 구매 보너스", orderId);
        
        // 처리 완료 표시
        savedHistory.markAsProcessed();
        rewardHistoryRepository.save(savedHistory);
        
        log.info("첫 구매 보너스 지급 완료: 사용자 ID={}, 주문 ID={}, 포인트={}", 
                user.getId(), orderId, policy.getPoints());
        
        return RewardHistoryResponse.from(savedHistory);
    }

    /**
     * 리워드 히스토리 조회
     */
    public Page<RewardHistoryResponse> getRewardHistory(UserDetails userDetails, int page, int size) {
        User user = validateUser(userDetails);
        
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<RewardHistory> historyPage = rewardHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        return historyPage.map(RewardHistoryResponse::from);
    }

    /**
     * 리워드 정책 조회
     */
    public List<RewardPolicyResponse> getRewardPolicies() {
        List<RewardPolicy> policies = rewardPolicyRepository.findValidPolicies();
        return policies.stream()
                .map(RewardPolicyResponse::from)
                .toList();
    }

    /**
     * 미처리 리워드 처리 (스케줄러용)
     */
    @Transactional
    public void processPendingRewards() {
        List<RewardHistory> pendingRewards = rewardHistoryRepository.findByIsProcessedFalseOrderByCreatedAtAsc();
        
        for (RewardHistory reward : pendingRewards) {
            try {
                // 포인트 적립 처리
                pointService.earnPoints(reward.getUser(), reward.getPoints(), 
                        "리워드 지급: " + reward.getDescription(), reward.getRelatedId());
                
                // 처리 완료 표시
                reward.markAsProcessed();
                rewardHistoryRepository.save(reward);
                
                log.info("미처리 리워드 처리 완료: 히스토리 ID={}, 사용자 ID={}, 포인트={}", 
                        reward.getHistoryId(), reward.getUser().getId(), reward.getPoints());
                        
            } catch (Exception e) {
                log.error("미처리 리워드 처리 실패: 히스토리 ID={}, 사용자 ID={}", 
                        reward.getHistoryId(), reward.getUser().getId(), e);
            }
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private User validateUser(UserDetails userDetails) {
        return userRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    private User validateAdmin(UserDetails adminDetails) {
        User admin = userRepository.findByLoginId(adminDetails.getUsername())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new UserException(ErrorCode.ACCESS_DENIED);
        }
        
        return admin;
    }

    private RewardPolicy getValidPolicy(RewardType rewardType) {
        return rewardPolicyRepository.findValidPolicyByType(rewardType)
                .orElseThrow(() -> new UserException(ErrorCode.REWARD_POLICY_NOT_FOUND));
    }

    private RewardType getReviewRewardType(String reviewType) {
        return switch (reviewType.toUpperCase()) {
            case "PHOTO" -> RewardType.REVIEW_PHOTO;
            case "QUALITY" -> RewardType.REVIEW_QUALITY;
            default -> RewardType.REVIEW_WRITE;
        };
    }

    private boolean isAlreadyRewarded(Long relatedId, String relatedType, RewardType rewardType) {
        return rewardHistoryRepository.findByRelatedIdAndRelatedTypeAndRewardType(relatedId, relatedType, rewardType)
                .isPresent();
    }

    private void validateRewardLimits(User user, RewardType rewardType) {
        RewardPolicy policy = getValidPolicy(rewardType);
        LocalDateTime now = LocalDateTime.now();
        
        // 일일 제한 확인
        if (policy.getDailyLimit() != null) {
            Long dailyCount = rewardHistoryRepository.countDailyRewardsByUserAndType(user, rewardType, now);
            if (dailyCount >= policy.getDailyLimit()) {
                throw new UserException(ErrorCode.DAILY_REWARD_LIMIT_EXCEEDED);
            }
        }
        
        // 월간 제한 확인
        if (policy.getMonthlyLimit() != null) {
            Long monthlyCount = rewardHistoryRepository.countMonthlyRewardsByUserAndType(user, rewardType, now);
            if (monthlyCount >= policy.getMonthlyLimit()) {
                throw new UserException(ErrorCode.MONTHLY_REWARD_LIMIT_EXCEEDED);
            }
        }
    }

    private boolean hasReceivedBirthdayRewardThisYear(User user, int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59);
        
        Page<RewardHistory> birthdayRewards = rewardHistoryRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startOfYear, endOfYear, PageRequest.of(0, 1));
        
        return birthdayRewards.hasContent() && 
               birthdayRewards.getContent().get(0).getRewardType() == RewardType.BIRTHDAY;
    }

    private boolean hasReceivedFirstPurchaseReward(User user) {
        return rewardHistoryRepository.findByUserAndRewardTypeOrderByCreatedAtDesc(user, RewardType.FIRST_PURCHASE, PageRequest.of(0, 1))
                .hasContent();
    }
}
