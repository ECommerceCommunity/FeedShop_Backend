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

import java.util.List;
import java.util.ArrayList;

/**
 * 이벤트 생성 서비스
 * 
 * <p>이벤트와 관련된 상세 정보, 보상 정보를 함께 생성합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventCreateService {

    private final EventRepository eventRepository;
    private final EventValidator eventValidator;

    /**
     * 이벤트를 생성합니다.
     * 
     * @param requestDto 이벤트 생성 요청 DTO
     * @return 생성된 이벤트 응답 DTO
     */
    public EventCreateResponseDto createEvent(EventCreateRequestDto requestDto) {
        log.info("이벤트 생성 시작 - 제목: {}", requestDto.getTitle());
        
        // 검증 수행
        eventValidator.validateEventCreateRequest(requestDto);
        log.debug("이벤트 생성 요청 검증 완료");

        // Event 엔티티 생성 (팩토리 메서드 활용)
        Event event = Event.builder()
                .type(requestDto.getType())
                .maxParticipants(requestDto.getMaxParticipants())
                .status(EventStatus.UPCOMING)
                .build();

        // EventDetail 엔티티 생성 (팩토리 메서드 활용)
        EventDetail eventDetail = EventDetail.createForEvent(event, 
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getParticipationMethod(),
                requestDto.getSelectionCriteria(),
                requestDto.getPrecautions());
        
        // 날짜 정보 설정
        eventDetail = EventDetail.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .participationMethod(requestDto.getParticipationMethod())
                .selectionCriteria(requestDto.getSelectionCriteria())
                .precautions(requestDto.getPrecautions())
                .purchaseStartDate(requestDto.getPurchaseStartDate())
                .purchaseEndDate(requestDto.getPurchaseEndDate())
                .eventStartDate(requestDto.getEventStartDate())
                .eventEndDate(requestDto.getEventEndDate())
                .announcement(requestDto.getAnnouncement())
                .build();
        eventDetail.setEvent(event);

        // EventReward 엔티티들 생성 (팩토리 메서드 활용)
        List<EventReward> eventRewards = createEventRewards(requestDto.getRewards());
        log.info("보상 정보 생성 완료 - 보상 개수: {}", eventRewards.size());

        // 연관관계 설정
        event.setEventDetail(eventDetail);
        event.setRewards(eventRewards);

        // 저장
        Event savedEvent = eventRepository.save(event);
        log.info("이벤트 생성 완료 - ID: {}, 제목: {}", savedEvent.getId(), requestDto.getTitle());

        // 응답 DTO 생성
        return EventCreateResponseDto.builder()
                .eventId(savedEvent.getId())
                .title(requestDto.getTitle())
                .type(savedEvent.getType().name().toLowerCase())
                .status(savedEvent.getStatus().name().toLowerCase())
                .maxParticipants(savedEvent.getMaxParticipants())
                .createdAt(savedEvent.getCreatedAt())
                .build();
    }

    /**
     * 보상 엔티티들을 생성합니다.
     * 
     * @param rewards 보상 요청 DTO 리스트
     * @return 생성된 EventReward 엔티티 리스트
     */
    private List<EventReward> createEventRewards(List<EventCreateRequestDto.EventRewardRequestDto> rewards) {
        List<EventReward> eventRewards = new ArrayList<>();
        
        for (int i = 0; i < rewards.size(); i++) {
            EventCreateRequestDto.EventRewardRequestDto rewardDto = rewards.get(i);
            
            log.debug("보상 {} 생성 중 - 조건값: {}, 보상내용: {}", 
                    i + 1, rewardDto.getConditionValue(), rewardDto.getRewardValue());
            
            Integer maxRecipients = calculateMaxRecipients(rewardDto.getConditionValue());
            
            // 팩토리 메서드 활용 (빌더 패턴 기반)
            EventReward eventReward = EventReward.createForEvent(null, rewardDto.getConditionValue(), rewardDto.getRewardValue(), maxRecipients);
            
            eventRewards.add(eventReward);
            
            log.debug("보상 {} 생성 완료 - 최대수령자수: {}", i + 1, maxRecipients);
        }
        
        return eventRewards;
    }

    /**
     * 조건값에 따라 최대 수령자 수를 계산합니다.
     * 
     * @param conditionValue 조건값
     * @return 최대 수령자 수
     */
    private Integer calculateMaxRecipients(String conditionValue) {
        try {
            // 숫자인 경우 (등수 조건)
            int rank = Integer.parseInt(conditionValue);
            log.debug("등수 조건 감지 - 등수: {}, 최대수령자수: {}", rank, 1);
            return 1;
        } catch (NumberFormatException e) {
            // 문자열 조건인 경우
            switch (conditionValue.toLowerCase()) {
                case "participation":
                    log.debug("참여자 조건 감지 - 최대수령자수: 무제한");
                    return null; // 무제한
                case "voters":
                case "views":
                case "likes":
                    log.debug("TOP 조건 감지 - 조건: {}, 최대수령자수: {}", conditionValue, 10);
                    return 10; // TOP 10
                case "random":
                    log.debug("랜덤 조건 감지 - 최대수령자수: {}", 5);
                    return 5; // 랜덤 5명
                default:
                    log.warn("알 수 없는 조건값: {} - 기본값 1 사용", conditionValue);
                    return 1;
            }
        }
    }
} 