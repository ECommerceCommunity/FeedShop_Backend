package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventCreateResponseDto;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Slf4j
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
        log.info("이벤트 생성 시작: {}", requestDto.getTitle());
        
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

                .build();
        
        // EventReward 엔티티들 생성
        List<EventReward> eventRewards = createEventRewards(requestDto.getRewards());
        
        // 연관관계 설정
        event.setEventDetail(eventDetail);
        event.setRewards(eventRewards);
        
        // 상태 자동 계산 및 업데이트
        event.updateStatusAutomatically();
        
        // 저장
        Event savedEvent = eventRepository.save(event);
        
        log.info("이벤트 생성 완료: ID={}, 제목={}", savedEvent.getId(), savedEvent.getEventDetail().getTitle());
        
        return EventCreateResponseDto.of(
                savedEvent.getId(),
                savedEvent.getEventDetail().getTitle(),
                savedEvent.getType().name().toLowerCase(),
                savedEvent.getStatus().name().toLowerCase(),
                savedEvent.getMaxParticipants(),
                savedEvent.getCreatedBy()
        );
    }

    /**
     * EventReward 엔티티 리스트 생성
     */
    private List<EventReward> createEventRewards(List<EventCreateRequestDto.EventRewardRequestDto> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<EventReward> eventRewards = new ArrayList<>();
        for (EventCreateRequestDto.EventRewardRequestDto rewardDto : rewards) {
            EventReward eventReward = EventReward.builder()
                    .conditionValue(rewardDto.getConditionValue())
                    .rewardValue(rewardDto.getRewardValue())
                    .maxRecipients(calculateMaxRecipients(rewardDto.getConditionValue()))
                    .build();
            eventRewards.add(eventReward);
        }
        return eventRewards;
    }

    /**
     * 조건에 따른 최대 수혜자 수 계산
     */
    private Integer calculateMaxRecipients(String conditionValue) {
        if (conditionValue == null) {
            return 1;
        }
        
        // 등수 조건인 경우
        try {
            int rank = Integer.parseInt(conditionValue);
            return 1; // 각 등수당 1명
        } catch (NumberFormatException e) {
            // 특별 조건인 경우
            switch (conditionValue.toLowerCase()) {
                case "participation":
                    return 10; // 참여자 중 랜덤 10명
                case "voters":
                case "views":
                case "likes":
                    return 5; // TOP 5명
                case "random":
                    return 3; // 랜덤 3명
                default:
                    return 1;
            }
        }
    }


} 