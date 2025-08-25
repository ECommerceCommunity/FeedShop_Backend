package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이벤트 결과 상세 정보 엔티티
 * 
 * <p>이벤트 결과에서 개별 참여자의 순위, 점수, 리워드 정보를 저장합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Entity
@Table(name = "event_result_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventResultDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private EventResult eventResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rank_position")
    private Integer rankPosition; // 순위 (1, 2, 3...)

    @Column(name = "vote_count")
    private Long voteCount; // 획득한 투표 수

    @Column(name = "points_earned")
    private Integer pointsEarned; // 획득한 포인트

    @Column(name = "badge_points_earned")
    private Integer badgePointsEarned; // 획득한 뱃지 점수

    @Column(name = "coupon_code")
    private String couponCode; // 할인쿠폰 코드

    @Column(name = "coupon_description")
    private String couponDescription; // 할인쿠폰 설명

    @Column(name = "reward_processed")
    @Builder.Default
    private Boolean rewardProcessed = false; // 리워드 지급 처리 여부

    @Column(name = "reward_processed_at")
    private LocalDateTime rewardProcessedAt; // 리워드 지급 처리 시간

    /**
     * 배틀 우승자 결과 상세 생성
     */
    public static EventResultDetail createBattleWinner(EventResult eventResult, User user, 
                                                     Long voteCount, Integer points, Integer badgePoints,
                                                     String couponCode, String couponDescription) {
        return EventResultDetail.builder()
                .eventResult(eventResult)
                .user(user)
                .rankPosition(1) // 배틀은 1등만
                .voteCount(voteCount)
                .pointsEarned(points)
                .badgePointsEarned(badgePoints)
                .couponCode(couponCode)
                .couponDescription(couponDescription)
                .build();
    }

    /**
     * 랭킹 결과 상세 생성
     */
    public static EventResultDetail createRankingResult(EventResult eventResult, User user, 
                                                      Integer rankPosition, Long voteCount, 
                                                      Integer points, Integer badgePoints,
                                                      String couponCode, String couponDescription) {
        return EventResultDetail.builder()
                .eventResult(eventResult)
                .user(user)
                .rankPosition(rankPosition)
                .voteCount(voteCount)
                .pointsEarned(points)
                .badgePointsEarned(badgePoints)
                .couponCode(couponCode)
                .couponDescription(couponDescription)
                .build();
    }

    /**
     * 리워드 지급 완료 처리
     */
    public void markRewardAsProcessed() {
        this.rewardProcessed = true;
        this.rewardProcessedAt = LocalDateTime.now();
    }

    /**
     * 연관관계 설정
     */
    public void setEventResult(EventResult eventResult) {
        this.eventResult = eventResult;
    }
}
