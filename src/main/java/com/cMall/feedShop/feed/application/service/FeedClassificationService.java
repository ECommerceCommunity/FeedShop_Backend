package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.EventResultDetail;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 피드 분류 서비스
 * 이벤트 결과에 따라 피드를 자동으로 분류합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedClassificationService {

    private final EventResultRepository eventResultRepository;
    private final FeedRepository feedRepository;

    /**
     * 이벤트 결과 발표 후 피드 분류 처리
     * 
     * @param eventId 이벤트 ID
     */
    public void classifyFeedsAfterEventResult(Long eventId) {
        log.info("이벤트 결과에 따른 피드 분류 시작 - eventId: {}", eventId);
        
        EventResult eventResult = eventResultRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 결과를 찾을 수 없습니다: " + eventId));
        
        // 승자/순위에 포함된 피드 ID들 추출
        Set<Long> winnerFeedIds = eventResult.getResultDetails().stream()
                .map(EventResultDetail::getFeedId)
                .collect(Collectors.toSet());
        
        // 해당 이벤트의 모든 피드 조회
        List<Feed> eventFeeds = feedRepository.findByEventId(eventId);
        
        for (Feed feed : eventFeeds) {
            if (winnerFeedIds.contains(feed.getId())) {
                // 승자/순위에 포함된 피드는 RANKING 피드로 변경
                feed.updateFeedType(FeedType.RANKING);
                log.info("피드를 RANKING으로 분류 - feedId: {}, eventId: {}", feed.getId(), eventId);
            } else {
                // 승자/순위에 포함되지 않은 피드는 DAILY 피드로 변경
                feed.updateFeedType(FeedType.DAILY);
                log.info("피드를 DAILY로 분류 - feedId: {}, eventId: {}", feed.getId(), eventId);
            }
        }
        
        log.info("이벤트 결과에 따른 피드 분류 완료 - eventId: {}, 총 피드 수: {}", eventId, eventFeeds.size());
    }

    /**
     * 특정 피드의 분류 상태 확인
     * 
     * @param feedId 피드 ID
     * @return 피드 타입
     */
    @Transactional(readOnly = true)
    public FeedType getFeedClassification(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다: " + feedId));
        
        return feed.getFeedType();
    }
}
