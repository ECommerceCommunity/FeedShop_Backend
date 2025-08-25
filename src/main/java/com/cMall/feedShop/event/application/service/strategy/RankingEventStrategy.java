package com.cMall.feedShop.event.application.service.strategy;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.EventResultDetail;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 랭킹 이벤트 전략 구현체
 * 
 * <p>랭킹 이벤트의 특화된 로직을 처리합니다.</p>
 * <ul>
 *   <li>투표 수 기준으로 정렬</li>
 *   <li>TOP 3 선정</li>
 *   <li>순위별 리워드 지급</li>
 * </ul>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingEventStrategy implements EventStrategy {

    private final FeedVoteRepository feedVoteRepository;

    @Override
    public EventType getEventType() {
        return EventType.RANKING;
    }

    @Override
    public EventResult calculateResult(Event event, List<Feed> participants) {
        log.info("랭킹 이벤트 결과 계산 시작 - eventId: {}, 참여자 수: {}", event.getId(), participants.size());
        
        if (participants.isEmpty()) {
            throw new IllegalStateException("랭킹 이벤트에 참여자가 없습니다.");
        }
        
        // 투표 수 기준으로 정렬 (내림차순)
        participants.sort((f1, f2) -> Long.compare(getVoteCount(f2.getId()), getVoteCount(f1.getId())));
        
        // TOP 3 선정
        List<EventResultDetail> top3Results = new ArrayList<>();
        long totalVotes = 0;
        
        for (int i = 0; i < Math.min(3, participants.size()); i++) {
            Feed feed = participants.get(i);
            top3Results.add(createRankingResultDetail(feed, event, i + 1));
            totalVotes += getVoteCount(feed.getId());
        }
        
        // 이벤트 결과 생성
        EventResult eventResult = EventResult.createForEvent(
                event,
                EventResult.ResultType.RANKING_TOP3,
                participants.size(),
                totalVotes
        );
        
        // TOP 3 상세 정보 추가
        top3Results.forEach(eventResult::addResultDetail);
        
        log.info("랭킹 이벤트 결과 계산 완료 - TOP 3 수: {}", top3Results.size());
        return eventResult;
    }

    @Override
    public boolean canParticipate(Event event, User user) {
        // 랭킹 이벤트는 모든 사용자가 참여 가능
        return true;
    }

    @Override
    public void validateParticipation(Event event, Feed feed) {
        // 랭킹 이벤트 참여 검증
        if (feed == null) {
            throw new IllegalArgumentException("피드 정보가 필요합니다.");
        }
        
        if (feed.getEvent() == null || !feed.getEvent().getId().equals(event.getId())) {
            throw new IllegalArgumentException("해당 이벤트에 참여하는 피드가 아닙니다.");
        }
    }

    @Override
    public EventParticipantInfo createParticipant(Event event, User user, Feed feed) {
        validateParticipation(event, feed);
        
        return new EventParticipantInfo(
                user.getId(),
                feed.getId(),
                "PARTICIPATING",
                "{\"currentRank\": null, \"voteCount\": 0}"
        );
    }

    /**
     * 랭킹 결과 상세 정보 생성
     */
    private EventResultDetail createRankingResultDetail(Feed feed, Event event, int rank) {
        // 이벤트 리워드에서 해당 순위 리워드 정보 조회
        EventReward rankReward = event.getRewards().stream()
                .filter(reward -> String.valueOf(rank).equals(reward.getConditionValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(rank + "등 리워드 정보를 찾을 수 없습니다."));
        
        // 리워드 파싱
        RewardInfo rewardInfo = parseRewardValue(rankReward.getRewardValue());
        
        return EventResultDetail.createRankingResult(
                null, // EventResult는 나중에 설정
                feed.getUser(),
                rank,
                getVoteCount(feed.getId()),
                rewardInfo.getPoints(),
                rewardInfo.getBadgePoints(),
                rewardInfo.getCouponCode(),
                rewardInfo.getCouponDescription()
        );
    }

    /**
     * 피드 투표 수 조회
     */
    private long getVoteCount(Long feedId) {
        return feedVoteRepository.countByFeedId(feedId);
    }

    /**
     * 리워드 값 파싱
     */
    private RewardInfo parseRewardValue(String rewardValue) {
        RewardInfo rewardInfo = new RewardInfo();
        
        if (rewardValue != null) {
            String[] parts = rewardValue.split(",");
            for (String part : parts) {
                String[] keyValue = part.trim().split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    
                    switch (key) {
                        case "포인트":
                            rewardInfo.setPoints(Integer.parseInt(value));
                            break;
                        case "뱃지점수":
                            rewardInfo.setBadgePoints(Integer.parseInt(value));
                            break;
                        case "쿠폰":
                            rewardInfo.setCouponCode(value);
                            rewardInfo.setCouponDescription(value);
                            break;
                    }
                }
            }
        }
        
        return rewardInfo;
    }

    /**
     * 리워드 정보
     */
    private static class RewardInfo {
        private Integer points = 0;
        private Integer badgePoints = 0;
        private String couponCode = "";
        private String couponDescription = "";
        
        // Getters and Setters
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
        public Integer getBadgePoints() { return badgePoints; }
        public void setBadgePoints(Integer badgePoints) { this.badgePoints = badgePoints; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
        public String getCouponDescription() { return couponDescription; }
        public void setCouponDescription(String couponDescription) { this.couponDescription = couponDescription; }
    }
}
