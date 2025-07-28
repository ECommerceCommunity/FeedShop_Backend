package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventCreateResponseDto;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventCreateService {
    private final EventRepository eventRepository;
    private final EventValidator eventValidator;

    /**
     * 이벤트 생성
     */
    @Transactional
    public EventCreateResponseDto createEvent(EventCreateRequestDto requestDto) {
        // 입력값 검증
        eventValidator.validateEventCreateRequest(requestDto);
        
        // Event 엔티티 생성 (상태는 자동 계산)
        Event event = Event.builder()
                .type(requestDto.getType())
                .status(EventStatus.UPCOMING) // 임시 상태, 나중에 자동 계산
                .maxParticipants(requestDto.getMaxParticipants())
                .createdBy(LocalDateTime.now())
                .build();
        
        // EventDetail 엔티티 생성
        EventDetail eventDetail = EventDetail.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .imageUrl(requestDto.getImageUrl())
                .participationMethod(requestDto.getParticipationMethod())
                .selectionCriteria(requestDto.getSelectionCriteria())
                .precautions(requestDto.getPrecautions())
                .purchaseStartDate(requestDto.getPurchaseStartDate())
                .purchaseEndDate(requestDto.getPurchaseEndDate())
                .eventStartDate(requestDto.getEventStartDate())
                .eventEndDate(requestDto.getEventEndDate())
                .announcement(requestDto.getAnnouncement())
                .rewards(requestDto.getRewards()) // 문자열로 저장
                .build();
        
        // 연관관계 설정
        event.setEventDetail(eventDetail);
        
        // 상태 자동 계산 및 업데이트
        event.updateStatusAutomatically();
        
        // 저장
        Event savedEvent = eventRepository.save(event);
        
        return EventCreateResponseDto.of(
                savedEvent.getId(),
                savedEvent.getEventDetail().getTitle(),
                savedEvent.getType().name().toLowerCase(),
                savedEvent.getStatus().name().toLowerCase(),
                savedEvent.getMaxParticipants(),
                savedEvent.getCreatedBy()
        );
    }
} 