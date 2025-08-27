package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.common.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Event 상태 관리 서비스
 * 
 * <p>이벤트의 상태를 자동으로 계산하고 업데이트하는 서비스입니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventStatusService {

    private final EventRepository eventRepository;

    /**
     * 모든 이벤트의 상태를 자동으로 업데이트
     * 스케줄러에서 호출됨
     */
    public void updateAllEventStatuses() {
        log.info("이벤트 상태 자동 업데이트 시작");
        
        try {
            // 삭제되지 않은 모든 이벤트 조회
            List<Event> events = eventRepository.findAll();
            
            if (events.isEmpty()) {
                log.debug("업데이트할 이벤트가 없습니다");
                return;
            }
            
            int updatedCount = 0;
            LocalDate today = TimeUtil.nowDate();
            
            for (Event event : events) {
                if (updateEventStatusIfNeeded(event, today)) {
                    updatedCount++;
                }
            }
            
            log.info("이벤트 상태 자동 업데이트 완료 - 총 {}개 중 {}개 업데이트", events.size(), updatedCount);
            
        } catch (Exception e) {
            log.error("이벤트 상태 자동 업데이트 중 오류 발생", e);
            throw new RuntimeException("이벤트 상태 업데이트 실패", e);
        }
    }

    /**
     * 특정 이벤트의 상태를 업데이트 (필요한 경우에만)
     * 
     * @param event 업데이트할 이벤트
     * @param currentDate 현재 날짜
     * @return 상태가 변경되었는지 여부
     */
    public boolean updateEventStatusIfNeeded(Event event, LocalDate currentDate) {
        EventStatus calculatedStatus = calculateEventStatus(event, currentDate);
        
        if (event.getStatus() != calculatedStatus) {
            log.debug("이벤트 상태 업데이트 - ID: {}, 기존: {} -> 새로운: {}", 
                    event.getId(), event.getStatus(), calculatedStatus);
            
            event.updateStatus(calculatedStatus);
            return true;
        }
        
        return false;
    }

    /**
     * 특정 이벤트의 상태를 업데이트
     * 
     * @param eventId 업데이트할 이벤트 ID
     * @return 상태가 변경되었는지 여부
     */
    public boolean updateEventStatus(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));
        
        return updateEventStatusIfNeeded(event, TimeUtil.nowDate());
    }

    /**
     * 이벤트 상태 계산
     * 
     * @param event 이벤트
     * @param currentDate 현재 날짜
     * @return 계산된 이벤트 상태
     */
    public EventStatus calculateEventStatus(Event event, LocalDate currentDate) {
        if (event.getEventDetail() == null || 
            event.getEventDetail().getEventStartDate() == null || 
            event.getEventDetail().getEventEndDate() == null) {
            return event.getStatus(); // 기본값 반환
        }
        
        LocalDate startDate = event.getEventDetail().getEventStartDate();
        LocalDate endDate = event.getEventDetail().getEventEndDate();
        
        if (currentDate.isBefore(startDate)) {
            return EventStatus.UPCOMING;
        } else if (currentDate.isAfter(endDate)) {
            return EventStatus.ENDED;
        } else {
            return EventStatus.ONGOING;
        }
    }

    /**
     * 이벤트 참여 가능 여부 확인
     * 
     * @param event 이벤트
     * @return 참여 가능 여부
     */
    public boolean isEventParticipatable(Event event) {
        if (event.getEventDetail() == null || 
            event.getEventDetail().getEventStartDate() == null || 
            event.getEventDetail().getEventEndDate() == null) {
            return false;
        }
        
        LocalDate today = TimeUtil.nowDate();
        LocalDate startDate = event.getEventDetail().getEventStartDate();
        LocalDate endDate = event.getEventDetail().getEventEndDate();
        
        // 시작일부터 종료일까지 참여 가능 (종료일 포함)
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * 이벤트 상태가 최신인지 확인
     * 
     * @param event 이벤트
     * @return 상태가 최신인지 여부
     */
    public boolean isEventStatusUpToDate(Event event) {
        EventStatus calculatedStatus = calculateEventStatus(event, TimeUtil.nowDate());
        return event.getStatus() == calculatedStatus;
    }
}
