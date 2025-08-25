package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedRewardEventSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedRewardEventResponseDto;
import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.feed.domain.repository.FeedRewardEventRepository;
import com.cMall.feedShop.user.domain.enums.RewardType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 피드 리워드 이벤트 관리 서비스
 * 관리자가 리워드 이벤트를 조회하고 관리할 수 있는 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedRewardEventManagementService {

    private final FeedRewardEventRepository feedRewardEventRepository;
    private final FeedRewardEventProcessor feedRewardEventProcessor;

    /**
     * 리워드 이벤트 목록 조회 (페이지네이션)
     */
    public Page<FeedRewardEventResponseDto> getRewardEvents(FeedRewardEventSearchRequest request) {
        log.info("리워드 이벤트 목록 조회 - request: {}", request);
        
        if (!request.isValid()) {
            throw new IllegalArgumentException("잘못된 요청 파라미터입니다");
        }
        
        Pageable pageable = request.toPageable();
        
        // 필터링 조건에 따른 조회
        Page<FeedRewardEvent> events;
        if (request.getUserId() != null) {
            events = feedRewardEventRepository.findByUserOrderByCreatedAtDesc(
                    request.getUserId(), pageable);
        } else if (request.getFeedId() != null) {
            events = feedRewardEventRepository.findByFeedOrderByCreatedAtDesc(
                    request.getFeedId(), pageable);
        } else if (request.getRewardType() != null) {
            events = feedRewardEventRepository.findByRewardTypeOrderByCreatedAtDesc(
                    request.getRewardType(), pageable);
        } else if (request.getEventStatus() != null) {
            events = feedRewardEventRepository.findByEventStatusOrderByCreatedAtAsc(
                    request.getEventStatus(), pageable);
        } else {
            // 전체 조회
            events = feedRewardEventRepository.findAll(pageable);
        }
        
        // DTO 변환
        Page<FeedRewardEventResponseDto> response = events.map(FeedRewardEventResponseDto::from);
        
        log.info("리워드 이벤트 목록 조회 완료 - 총 {}개", response.getTotalElements());
        return response;
    }

    /**
     * 특정 사용자의 리워드 이벤트 조회
     */
    public List<FeedRewardEventResponseDto> getRewardEventsByUser(Long userId) {
        log.info("사용자별 리워드 이벤트 조회 - userId: {}", userId);
        
        List<FeedRewardEvent> events = feedRewardEventRepository.findByUserOrderByCreatedAtDesc(userId);
        List<FeedRewardEventResponseDto> response = events.stream()
                .map(FeedRewardEventResponseDto::from)
                .collect(Collectors.toList());
        
        log.info("사용자별 리워드 이벤트 조회 완료 - userId: {}, 이벤트 수: {}", userId, response.size());
        return response;
    }

    /**
     * 특정 피드의 리워드 이벤트 조회
     */
    public List<FeedRewardEventResponseDto> getRewardEventsByFeed(Long feedId) {
        log.info("피드별 리워드 이벤트 조회 - feedId: {}", feedId);
        
        List<FeedRewardEvent> events = feedRewardEventRepository.findByFeedIdOrderByCreatedAtDesc(feedId);
        List<FeedRewardEventResponseDto> response = events.stream()
                .map(FeedRewardEventResponseDto::from)
                .collect(Collectors.toList());
        
        log.info("피드별 리워드 이벤트 조회 완료 - feedId: {}, 이벤트 수: {}", feedId, response.size());
        return response;
    }

    /**
     * 리워드 이벤트 통계 조회
     */
    public Map<String, Object> getRewardEventStatistics() {
        log.info("리워드 이벤트 통계 조회");
        
        // 전체 이벤트 수
        long totalEvents = feedRewardEventRepository.count();
        
        // 상태별 이벤트 수
        long pendingEvents = feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.PENDING);
        long processingEvents = feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.PROCESSING);
        long processedEvents = feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.PROCESSED);
        long failedEvents = feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.FAILED);
        
        // 리워드 타입별 이벤트 수
        Map<RewardType, Long> eventsByType = feedRewardEventRepository.countByRewardType();
        
        // 총 지급된 포인트
        Integer totalPoints = feedRewardEventRepository.sumPointsByEventStatus(FeedRewardEvent.EventStatus.PROCESSED);
        
        Map<String, Object> statistics = Map.of(
                "totalEvents", totalEvents,
                "pendingEvents", pendingEvents,
                "processingEvents", processingEvents,
                "processedEvents", processedEvents,
                "failedEvents", failedEvents,
                "eventsByType", eventsByType,
                "totalPoints", totalPoints != null ? totalPoints : 0
        );
        
        log.info("리워드 이벤트 통계 조회 완료 - 총 이벤트: {}, 총 포인트: {}", totalEvents, totalPoints);
        return statistics;
    }

    /**
     * 일별 리워드 이벤트 통계 조회
     */
    public Map<String, Object> getDailyRewardEventStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("일별 리워드 이벤트 통계 조회 - startDate: {}, endDate: {}", startDate, endDate);
        
        // 일별 생성된 이벤트 수
        Map<String, Long> dailyCreatedEvents = feedRewardEventRepository.countDailyCreatedEvents(startDate, endDate);
        
        // 일별 처리된 이벤트 수
        Map<String, Long> dailyProcessedEvents = feedRewardEventRepository.countDailyProcessedEvents(startDate, endDate);
        
        // 일별 지급된 포인트
        Map<String, Integer> dailyPoints = feedRewardEventRepository.sumDailyPoints(startDate, endDate);
        
        Map<String, Object> statistics = Map.of(
                "dailyCreatedEvents", dailyCreatedEvents,
                "dailyProcessedEvents", dailyProcessedEvents,
                "dailyPoints", dailyPoints
        );
        
        log.info("일별 리워드 이벤트 통계 조회 완료");
        return statistics;
    }

    /**
     * 수동으로 특정 이벤트 처리
     */
    @Transactional
    public void processEventManually(Long eventId) {
        log.info("수동 리워드 이벤트 처리 시작 - eventId: {}", eventId);
        
        try {
            feedRewardEventProcessor.processSpecificEvent(eventId);
            log.info("수동 리워드 이벤트 처리 완료 - eventId: {}", eventId);
        } catch (Exception e) {
            log.error("수동 리워드 이벤트 처리 실패 - eventId: {}", eventId, e);
            throw new RuntimeException("리워드 이벤트 처리에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 실패한 이벤트 재처리
     */
    @Transactional
    public void retryFailedEvents() {
        log.info("실패한 리워드 이벤트 재처리 시작");
        
        try {
            feedRewardEventProcessor.retryFailedRewardEvents();
            log.info("실패한 리워드 이벤트 재처리 완료");
        } catch (Exception e) {
            log.error("실패한 리워드 이벤트 재처리 실패", e);
            throw new RuntimeException("실패한 이벤트 재처리에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 특정 이벤트 상세 조회
     */
    public FeedRewardEventResponseDto getRewardEventDetail(Long eventId) {
        log.info("리워드 이벤트 상세 조회 - eventId: {}", eventId);
        
        FeedRewardEvent event = feedRewardEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("리워드 이벤트를 찾을 수 없습니다: " + eventId));
        
        FeedRewardEventResponseDto response = FeedRewardEventResponseDto.from(event);
        
        log.info("리워드 이벤트 상세 조회 완료 - eventId: {}", eventId);
        return response;
    }
}
