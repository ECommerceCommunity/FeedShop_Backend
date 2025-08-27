package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.common.util.TimeUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUpdateService {
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final EventImageService eventImageService;
    private final EventStatusService eventStatusService;

    /**
     * 이벤트 수정 비즈니스 로직
     */
    @Transactional
    public void updateEvent(EventUpdateRequestDto dto) {
        updateEventWithImages(dto, null);
    }

    /**
     * 이미지와 함께 이벤트 수정
     */
    @Transactional
    public void updateEventWithImages(EventUpdateRequestDto dto, List<MultipartFile> images) {
        Event event = eventRepository.findDetailById(dto.getEventId())
                .orElseThrow(() -> new EventNotFoundException(dto.getEventId()));
        
        // 이벤트 기본 정보 업데이트 (영속성 유지)
        event.updateFromDto(dto);
        
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
        
        // 이미지 업데이트 처리
        if (images != null && !images.isEmpty()) {
            log.info("이벤트 이미지 업데이트 시작 - 이미지 개수: {}", images.size());
            eventImageService.replaceImages(event, images);
            log.info("이벤트 이미지 업데이트 완료");
        }

        // 상태 자동 업데이트
        eventStatusService.updateEventStatusIfNeeded(event, TimeUtil.nowDate());
        
        // JPA Dirty Checking으로 자동 변경사항 감지 및 DB 반영
        // @Transactional에 의해 트랜잭션 종료 시 자동 커밋됨
        // eventRepository.save(event); // 불필요 - 영속 상태에서 자동 처리됨
    }
} 