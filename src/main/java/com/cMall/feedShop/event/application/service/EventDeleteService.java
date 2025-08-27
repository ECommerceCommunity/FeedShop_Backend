package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventDeleteService {
    private final EventRepository eventRepository;
    private final EventImageService eventImageService;

    /**
     * 이벤트 소프트 딜리트(삭제)
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        
        // 이미지 파일들 삭제
        if (event.getImages() != null && !event.getImages().isEmpty()) {
            log.info("이벤트 이미지 삭제 시작 - eventId: {}, 이미지 개수: {}", eventId, event.getImages().size());
            eventImageService.deleteAllImages(event);
            log.info("이벤트 이미지 삭제 완료");
        }
        
        eventRepository.delete(event);
    }
} 