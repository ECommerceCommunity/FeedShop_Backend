package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventUpdateService {
    private final EventRepository eventRepository;

    /**
     * 이벤트 수정 비즈니스 로직
     */
    @Transactional
    public void updateEvent(EventUpdateRequestDto dto) {
        Event event = eventRepository.findDetailById(dto.getEventId())
                .orElseThrow(() -> new EventNotFoundException(dto.getEventId()));
        
        // 이벤트 기본 정보 업데이트 (영속성 유지)
        event.update(dto.getType(), dto.getMaxParticipants());
        
        // EventDetail 업데이트는 별도로 처리
        if (event.getEventDetail() != null) {
            event.getEventDetail().updateFromDto(dto);
        }
        
        // 상태 자동 업데이트
        event.updateStatusAutomatically();
        
        // 저장 (변경사항이 자동으로 감지됨)
        eventRepository.save(event);
    }
} 