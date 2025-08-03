package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.domain.enums.RewardConditionType;
import com.cMall.feedShop.common.util.TimeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EventValidator {

    /**
     * 이벤트 생성 요청 검증
     */
    public void validateEventCreateRequest(EventCreateRequestDto requestDto) {
        validateRequiredFields(requestDto);
        validateDateOrder(requestDto);
        validateRewards(requestDto.getRewards());
    }

    /**
     * 필수 필드 검증
     */
    private void validateRequiredFields(EventCreateRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("이벤트 제목은 필수입니다.");
        }
        if (requestDto.getDescription() == null || requestDto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("이벤트 설명은 필수입니다.");
        }
        if (requestDto.getParticipationMethod() == null || requestDto.getParticipationMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("참여 방법은 필수입니다.");
        }
        if (requestDto.getSelectionCriteria() == null || requestDto.getSelectionCriteria().trim().isEmpty()) {
            throw new IllegalArgumentException("선정 기준은 필수입니다.");
        }
        if (requestDto.getPrecautions() == null || requestDto.getPrecautions().trim().isEmpty()) {
            throw new IllegalArgumentException("주의사항은 필수입니다.");
        }
        if (requestDto.getMaxParticipants() == null || requestDto.getMaxParticipants() < 1) {
            throw new IllegalArgumentException("최대 참여자 수는 1명 이상이어야 합니다.");
        }
    }

    /**
     * 날짜 순서 검증
     */
    private void validateDateOrder(EventCreateRequestDto requestDto) {
        // 1. 구매/이벤트 기간 순서 검증
        validatePurchaseAndEventDateOrder(
            requestDto.getPurchaseStartDate(),
            requestDto.getPurchaseEndDate(),
            requestDto.getEventStartDate(),
            requestDto.getEventEndDate()
        );
        
        // 2. 발표일 검증은 조건부로 실행
        if (requestDto.getEventEndDate() != null) { // eventEnd가 유효한 경우에만 발표일 검증
            validateEventAndAnnouncementDateOrder(
                requestDto.getEventEndDate(),
                requestDto.getAnnouncement()
            );
        }
    }

    /**
     * 구매 기간과 이벤트 기간 순서 검증
     */
    private void validatePurchaseAndEventDateOrder(LocalDate purchaseStart, LocalDate purchaseEnd,
                                                   LocalDate eventStart, LocalDate eventEnd) {
        // 날짜 순서 검증
        List<String> errors = new java.util.ArrayList<>();
        if (purchaseStart != null && eventStart != null && eventStart.isBefore(purchaseStart)) {
            errors.add("이벤트 시작일은 구매 시작일 이후나 당일이어야 합니다.");
        }
        if (purchaseEnd != null && eventEnd != null && eventEnd.isBefore(purchaseEnd)) {
            errors.add("이벤트 종료일은 구매 종료일 이후여야 합니다.");
        }
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0));
        }
    }

    /**
     * 이벤트 종료일과 발표일 순서 검증
     */
    private void validateEventAndAnnouncementDateOrder(LocalDate eventEnd, LocalDate announcement) {
        if (eventEnd != null && announcement != null && announcement.isBefore(eventEnd)) {
            throw new IllegalArgumentException("결과 발표일은 이벤트 종료일 이후여야 합니다.");
        }
    }

    /**
     * 보상 정보 검증
     */
    private void validateRewards(List<EventCreateRequestDto.EventRewardRequestDto> rewards) {
        // 개수 검증은 @Size 어노테이션으로 처리됨 (min=1, max=5)
        // 개별 보상의 비즈니스 로직 검증만 수행
        for (int i = 0; i < rewards.size(); i++) {
            EventCreateRequestDto.EventRewardRequestDto reward = rewards.get(i);
            validateReward(reward, i + 1);
        }
    }

    /**
     * 개별 보상 검증
     */
    private void validateReward(EventCreateRequestDto.EventRewardRequestDto reward, int index) {
        try {
            // 비즈니스 로직 검증만 수행 (기본 검증은 @NotBlank, @Size로 처리됨)
            reward.validate();
        } catch (IllegalArgumentException e) {
            // 인덱스 정보를 포함하여 예외 메시지 개선
            throw new IllegalArgumentException(index + "번째 " + e.getMessage());
        }
    }
} 