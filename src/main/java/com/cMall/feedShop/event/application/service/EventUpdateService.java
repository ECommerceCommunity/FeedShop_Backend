package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUpdateService {
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

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
        
        // rewards 업데이트 처리
        if (dto.getRewards() != null && !dto.getRewards().trim().isEmpty()) {
            try {
                // JSON 문자열을 List로 파싱
                List<EventCreateRequestDto.EventRewardRequestDto> rewardDtos = objectMapper.readValue(
                    dto.getRewards(), 
                    new TypeReference<List<EventCreateRequestDto.EventRewardRequestDto>>() {}
                );
                
                // 기존 rewards 삭제
                event.getRewards().clear();
                
                // 새로운 rewards 생성 및 추가 (정적 팩토리 메서드 활용)
                for (EventCreateRequestDto.EventRewardRequestDto rewardDto : rewardDtos) {
                    EventReward eventReward = EventReward.createForEvent(
                        event, 
                        rewardDto.getConditionValue(), 
                        rewardDto.getRewardValue()
                    );
                    event.getRewards().add(eventReward);
                }
                
                log.info("이벤트 보상 정보 업데이트 완료 - 보상 개수: {}", rewardDtos.size());
            } catch (Exception e) {
                log.error("보상 정보 파싱 실패: {}", e.getMessage());
                throw new RuntimeException("보상 정보 처리 중 오류가 발생했습니다.", e);
            }
        }
        
        // 상태 자동 업데이트
        event.updateStatusAutomatically();
        
        // 저장 (변경사항이 자동으로 감지됨)
        eventRepository.save(event);
    }
} 