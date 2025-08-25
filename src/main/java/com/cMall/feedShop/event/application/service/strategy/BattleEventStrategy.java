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
import java.util.Collections;
import java.util.List;

/**
 * 배틀 이벤트 전략 구현체
 * 
 * <p>배틀 이벤트의 특화된 로직을 처리합니다.</p>
 * <ul>
 *   <li>랜덤 매칭으로 2명씩 대결</li>
 *   <li>투표 수 비교로 우승자 선정</li>
 *   <li>동점 시 먼저 등록한 피드가 우승</li>
 * </ul>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BattleEventStrategy implements EventStrategy {

    private final FeedVoteRepository feedVoteRepository;

    @Override
    public EventType getEventType() {
        return EventType.BATTLE;
    }

    @Override
    public EventResult calculateResult(Event event, List<Feed> participants) {
        log.info("배틀 이벤트 결과 계산 시작 - eventId: {}, 참여자 수: {}", event.getId(), participants.size());
        
        if (participants.size() < 2) {
            throw new IllegalStateException("배틀 이벤트는 최소 2명의 참여자가 필요합니다.");
        }
        
        // 참여자들을 랜덤으로 2명씩 매칭
        List<BattleMatch> battleMatches = createBattleMatches(participants);
        
        // 각 매치에서 우승자 선정
        List<EventResultDetail> winners = new ArrayList<>();
        long totalVotes = 0;
        
        for (BattleMatch match : battleMatches) {
            Feed winner = determineBattleWinner(match.getParticipant1(), match.getParticipant2());
            winners.add(createBattleWinnerDetail(winner, event));
            totalVotes += getVoteCount(winner.getId());
        }
        
        // 이벤트 결과 생성
        EventResult eventResult = EventResult.createForEvent(
                event, 
                EventResult.ResultType.BATTLE_WINNER,
                participants.size(),
                totalVotes
        );
        
        // 우승자 상세 정보 추가
        winners.forEach(eventResult::addResultDetail);
        
        log.info("배틀 이벤트 결과 계산 완료 - 우승자 수: {}", winners.size());
        return eventResult;
    }

    @Override
    public boolean canParticipate(Event event, User user) {
        // 배틀 이벤트는 모든 사용자가 참여 가능
        return true;
    }

    @Override
    public void validateParticipation(Event event, Feed feed) {
        // 배틀 이벤트 참여 검증
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
                "{\"matchGroup\": null, \"matchPosition\": null}"
        );
    }

    /**
     * 배틀 매치 생성
     */
    private List<BattleMatch> createBattleMatches(List<Feed> participants) {
        List<BattleMatch> matches = new ArrayList<>();
        List<Feed> shuffledParticipants = new ArrayList<>(participants);
        Collections.shuffle(shuffledParticipants);
        
        // 2명씩 매칭
        for (int i = 0; i < shuffledParticipants.size() - 1; i += 2) {
            matches.add(new BattleMatch(
                    shuffledParticipants.get(i),
                    shuffledParticipants.get(i + 1)
            ));
        }
        
        // 홀수 명일 경우 마지막 참여자는 자동 우승
        if (shuffledParticipants.size() % 2 == 1) {
            Feed lastParticipant = shuffledParticipants.get(shuffledParticipants.size() - 1);
            matches.add(new BattleMatch(lastParticipant, null));
        }
        
        return matches;
    }

    /**
     * 배틀 우승자 결정
     */
    private Feed determineBattleWinner(Feed participant1, Feed participant2) {
        if (participant2 == null) {
            return participant1; // 상대가 없으면 자동 우승
        }
        
        long votes1 = getVoteCount(participant1.getId());
        long votes2 = getVoteCount(participant2.getId());
        
        if (votes1 > votes2) {
            return participant1;
        } else if (votes2 > votes1) {
            return participant2;
        } else {
            // 동점일 경우 먼저 등록한 피드가 우승
            return participant1.getCreatedAt().isBefore(participant2.getCreatedAt()) ? participant1 : participant2;
        }
    }

    /**
     * 배틀 우승자 상세 정보 생성
     */
    private EventResultDetail createBattleWinnerDetail(Feed winner, Event event) {
        // 이벤트 리워드에서 1등 리워드 정보 조회
        EventReward firstPlaceReward = event.getRewards().stream()
                .filter(reward -> "1".equals(reward.getConditionValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("1등 리워드 정보를 찾을 수 없습니다."));
        
        // 리워드 파싱
        RewardInfo rewardInfo = parseRewardValue(firstPlaceReward.getRewardValue());
        
        return EventResultDetail.createBattleWinner(
                null, // EventResult는 나중에 설정
                winner.getUser(),
                getVoteCount(winner.getId()),
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
     * 배틀 매치 정보
     */
    private static class BattleMatch {
        private final Feed participant1;
        private final Feed participant2;
        
        public BattleMatch(Feed participant1, Feed participant2) {
            this.participant1 = participant1;
            this.participant2 = participant2;
        }
        
        public Feed getParticipant1() { return participant1; }
        public Feed getParticipant2() { return participant2; }
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
