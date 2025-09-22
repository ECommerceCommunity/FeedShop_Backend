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
            log.info("리워드 업데이트 시작 - 원본 데이터: {}", dto.getRewards());
            try {
                // JSON 문자열을 List로 파싱
                List<EventCreateRequestDto.EventRewardRequestDto> rewardDtos = objectMapper.readValue(
                    dto.getRewards(), 
                    new TypeReference<List<EventCreateRequestDto.EventRewardRequestDto>>() {}
                );
                
                log.info("리워드 파싱 완료 - 파싱된 데이터: {}", rewardDtos);
                
                // 기존 rewards 삭제
                int originalRewardCount = event.getRewards().size();
                event.getRewards().clear();
                log.info("기존 리워드 {}개 삭제 완료", originalRewardCount);
                
                // 새로운 rewards 생성 및 추가 (정적 팩토리 메서드 활용)
                for (EventCreateRequestDto.EventRewardRequestDto rewardDto : rewardDtos) {
                    log.info("리워드 생성 중 - 조건값: {}, 보상내용: {}", 
                            rewardDto.getConditionValue(), rewardDto.getRewardValue());
                    
                    EventReward eventReward = EventReward.createForEvent(
                        event, 
                        rewardDto.getConditionValue(), 
                        rewardDto.getRewardValue()
                    );
                    event.getRewards().add(eventReward);
                    
                    log.info("리워드 생성 완료 - ID: {}, 조건값: {}, 보상내용: {}", 
                            eventReward.getId(), eventReward.getConditionValue(), eventReward.getRewardValue());
                }
                
                log.info("이벤트 보상 정보 업데이트 완료 - 보상 개수: {}", rewardDtos.size());
            } catch (Exception e) {
                log.error("보상 정보 파싱 실패: {}", e.getMessage(), e);
                throw new RuntimeException("보상 정보 처리 중 오류가 발생했습니다.", e);
            }
        } else {
            log.info("리워드 데이터가 없거나 비어있어 업데이트를 건너뜁니다.");
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
        
        log.info("이벤트 수정 완료 - eventId: {}", dto.getEventId());
    }
} 