package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.EventResultDetail;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.application.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 이벤트 리워드 지급 서비스
 * 
 * <p>이벤트 결과에 따른 실제 리워드 지급을 처리합니다.</p>
 * <p>포인트, 뱃지 점수, 쿠폰 시스템과 연동합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventRewardService {

    private final EventResultRepository eventResultRepository;
    private final PointService pointService;
    private final UserLevelService userLevelService;
    // TODO: 쿠폰 서비스 연동
    // private final CouponService couponService;

    /**
     * 이벤트 결과에 대한 리워드 지급
     * 
     * @param eventId 이벤트 ID
     * @return 지급 결과
     */
    public RewardProcessResult processEventRewards(Long eventId) {
        log.info("이벤트 리워드 지급 시작 - eventId: {}", eventId);
        
        // 1. 이벤트 결과 조회
        var eventResult = eventResultRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 결과를 찾을 수 없습니다: " + eventId));
        
        // 2. 각 참여자별 리워드 지급
        List<RewardDetail> rewardDetails = eventResult.getResultDetails().stream()
                .map(this::processParticipantReward)
                .toList();
        
        // 3. 결과 집계
        RewardProcessResult result = RewardProcessResult.builder()
                .eventId(eventId)
                .totalParticipants((long) rewardDetails.size())
                .successfulRewards(rewardDetails.stream().filter(detail -> detail.getSuccess()).count())
                .failedRewards(rewardDetails.stream().filter(detail -> !detail.getSuccess()).count())
                .rewardDetails(rewardDetails)
                .build();
        
        log.info("이벤트 리워드 지급 완료 - eventId: {}, 성공: {}, 실패: {}", 
                eventId, result.getSuccessfulRewards(), result.getFailedRewards());
        
        return result;
    }

    /**
     * 개별 참여자 리워드 지급
     */
    private RewardDetail processParticipantReward(EventResultDetail detail) {
        try {
            log.debug("참여자 리워드 지급 시작 - userId: {}, rankPosition: {}", 
                    detail.getUser().getId(), detail.getRankPosition());
            
            // 이미 지급된 경우 스킵
            if (detail.getRewardProcessed()) {
                log.debug("이미 지급된 리워드 스킵 - userId: {}", detail.getUser().getId());
                return RewardDetail.builder()
                        .userId(detail.getUser().getId())
                        .rankPosition(detail.getRankPosition())
                        .success(true)
                        .message("이미 지급된 리워드")
                        .build();
            }
            
            // 1. 포인트 지급
            if (detail.getPointsEarned() != null && detail.getPointsEarned() > 0) {
                pointService.earnPoints(detail.getUser(), detail.getPointsEarned(), 
                        "이벤트 " + detail.getRankPosition() + "등 리워드", null);
                log.debug("포인트 지급 완료 - userId: {}, points: {}", 
                        detail.getUser().getId(), detail.getPointsEarned());
            }
            
            // 2. 뱃지 점수 지급 (활동 기록으로 처리)
            if (detail.getBadgePointsEarned() != null && detail.getBadgePointsEarned() > 0) {
                userLevelService.recordActivity(detail.getUser().getId(), 
                        com.cMall.feedShop.user.domain.model.ActivityType.EVENT_WINNER,
                        "이벤트 " + detail.getRankPosition() + "등 리워드", 
                        detail.getEventResult().getEvent().getId(), "EVENT");
                log.debug("뱃지 점수 지급 완료 - userId: {}, badgePoints: {}", 
                        detail.getUser().getId(), detail.getBadgePointsEarned());
            }
            
            // 3. 쿠폰 지급 (TODO: 쿠폰 시스템 연동)
            if (detail.getCouponCode() != null && !detail.getCouponCode().isEmpty()) {
                // couponService.issueCoupon(detail.getUser().getId(), detail.getCouponCode());
                log.debug("쿠폰 지급 예정 - userId: {}, couponCode: {}", 
                        detail.getUser().getId(), detail.getCouponCode());
            }
            
            // 4. 지급 완료 처리
            detail.markRewardAsProcessed();
            
            return RewardDetail.builder()
                    .userId(detail.getUser().getId())
                    .rankPosition(detail.getRankPosition())
                    .pointsEarned(detail.getPointsEarned())
                    .badgePointsEarned(detail.getBadgePointsEarned())
                    .couponCode(detail.getCouponCode())
                    .success(true)
                    .message("리워드 지급 완료")
                    .build();
            
        } catch (Exception e) {
            log.error("리워드 지급 실패 - userId: {}, error: {}", detail.getUser().getId(), e.getMessage());
            
            return RewardDetail.builder()
                    .userId(detail.getUser().getId())
                    .rankPosition(detail.getRankPosition())
                    .success(false)
                    .message("리워드 지급 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 특정 참여자 리워드 재지급
     * 
     * @param eventId 이벤트 ID
     * @param userId 사용자 ID
     * @return 재지급 결과
     */
    public RewardDetail reprocessParticipantReward(Long eventId, Long userId) {
        log.info("참여자 리워드 재지급 시작 - eventId: {}, userId: {}", eventId, userId);
        
        var eventResult = eventResultRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 결과를 찾을 수 없습니다: " + eventId));
        
        var detail = eventResult.getResultDetails().stream()
                .filter(d -> d.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("참여자 결과를 찾을 수 없습니다: " + userId));
        
        // 지급 상태 초기화
        detail.markRewardAsProcessed();
        
        return processParticipantReward(detail);
    }

    /**
     * 리워드 지급 결과
     */
    @lombok.Builder
    @lombok.Getter
    public static class RewardProcessResult {
        private Long eventId;
        private Long totalParticipants;
        private Long successfulRewards;
        private Long failedRewards;
        private List<RewardDetail> rewardDetails;
    }

    /**
     * 개별 리워드 지급 상세
     */
    @lombok.Builder
    @lombok.Getter
    public static class RewardDetail {
        private Long userId;
        private Integer rankPosition;
        private Integer pointsEarned;
        private Integer badgePointsEarned;
        private String couponCode;
        private Boolean success;
        private String message;
    }
}
