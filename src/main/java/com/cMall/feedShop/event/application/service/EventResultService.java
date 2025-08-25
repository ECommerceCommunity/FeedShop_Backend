package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.*;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.application.service.PointService;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 이벤트 결과 계산 및 우승자 선정 서비스
 * 
 * <p>이벤트 종료 후 투표 결과를 바탕으로 우승자를 선정하고 리워드를 지급합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventResultService {

    private final EventRepository eventRepository;
    private final EventResultRepository eventResultRepository;
    private final FeedRepository feedRepository;
    private final FeedVoteRepository feedVoteRepository;
    private final PointService pointService;
    private final UserLevelService userLevelService;

    /**
     * 이벤트 결과 계산 및 발표
     */
    public EventResult calculateAndAnnounceEventResult(Long eventId) {
        log.info("이벤트 결과 계산 시작 - eventId: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));
        
        // 이미 결과가 발표된 이벤트인지 확인
        if (eventResultRepository.existsByEventId(eventId)) {
            throw new IllegalStateException("이미 결과가 발표된 이벤트입니다: " + eventId);
        }
        
        EventResult eventResult;
        
        switch (event.getType()) {
            case BATTLE:
                eventResult = calculateBattleResult(event);
                break;
            case RANKING:
                eventResult = calculateRankingResult(event);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 이벤트 타입입니다: " + event.getType());
        }
        
        // 결과 저장
        EventResult savedResult = eventResultRepository.save(eventResult);
        log.info("이벤트 결과 발표 완료 - eventId: {}, resultId: {}", eventId, savedResult.getId());
        
        return savedResult;
    }

    /**
     * 배틀 이벤트 결과 계산
     */
    private EventResult calculateBattleResult(Event event) {
        log.info("배틀 이벤트 결과 계산 - eventId: {}", event.getId());
        
        // 이벤트 참여 피드 조회
        List<Feed> eventFeeds = feedRepository.findByEventId(event.getId());
        
        if (eventFeeds.size() < 2) {
            throw new IllegalStateException("배틀 이벤트는 최소 2명의 참여자가 필요합니다.");
        }
        
        // 참여자들을 랜덤으로 2명씩 매칭
        List<BattleMatch> battleMatches = createBattleMatches(eventFeeds);
        
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
                eventFeeds.size(),
                totalVotes
        );
        
        // 우승자 상세 정보 추가
        winners.forEach(eventResult::addResultDetail);
        
        return eventResult;
    }

    /**
     * 랭킹 이벤트 결과 계산
     */
    private EventResult calculateRankingResult(Event event) {
        log.info("랭킹 이벤트 결과 계산 - eventId: {}", event.getId());
        
        // 이벤트 참여 피드 조회 및 투표 수 기준 정렬
        List<Feed> eventFeeds = feedRepository.findByEventId(event.getId());
        
        if (eventFeeds.isEmpty()) {
            throw new IllegalStateException("랭킹 이벤트에 참여자가 없습니다.");
        }
        
        // 투표 수 기준으로 정렬 (내림차순)
        eventFeeds.sort((f1, f2) -> Long.compare(getVoteCount(f2.getId()), getVoteCount(f1.getId())));
        
        // TOP 3 선정
        List<EventResultDetail> top3Results = new ArrayList<>();
        long totalVotes = 0;
        
        for (int i = 0; i < Math.min(3, eventFeeds.size()); i++) {
            Feed feed = eventFeeds.get(i);
            top3Results.add(createRankingResultDetail(feed, event, i + 1));
            totalVotes += getVoteCount(feed.getId());
        }
        
        // 이벤트 결과 생성
        EventResult eventResult = EventResult.createForEvent(
                event,
                EventResult.ResultType.RANKING_TOP3,
                eventFeeds.size(),
                totalVotes
        );
        
        // TOP 3 상세 정보 추가
        top3Results.forEach(eventResult::addResultDetail);
        
        return eventResult;
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
     * 
     * 예시: "포인트:1000, 뱃지점수:50, 쿠폰:20%할인쿠폰"
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
