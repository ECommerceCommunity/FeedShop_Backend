package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.exception.InvalidEventTypeException;
import org.springframework.stereotype.Component;

@Component
public class EventValidator {

    /**
     * 이벤트 생성 요청 검증
     * - 각 검증 로직을 개별 메서드로 분리하여 인지 복잡도(Cognitive Complexity)를 낮추고, 가독성과 유지보수성을 높임
     * - 모든 필수값/날짜/순서 검증을 한눈에 파악할 수 있도록 설계
     */
    public void validateEventCreateRequest(EventCreateRequestDto requestDto) {
        validateType(requestDto.getType());
        validateRequiredString(requestDto.getTitle(), "이벤트 제목은 필수입니다.");
        validateRequiredString(requestDto.getDescription(), "이벤트 설명은 필수입니다.");
        validateRequiredInteger(requestDto.getMaxParticipants(), "최대 참여자 수는 필수입니다.");
        validateRequiredDate(requestDto.getEventStartDate(), "이벤트 시작일은 필수입니다.");
        validateRequiredDate(requestDto.getEventEndDate(), "이벤트 종료일은 필수입니다.");
        validateRequiredString(requestDto.getRewards(), "이벤트 보상 정보는 필수입니다.");

        validateDateOrder(requestDto.getEventStartDate(), requestDto.getEventEndDate(), "이벤트 시작일은 종료일보다 이전이어야 합니다.");
        validateDateOrder(requestDto.getPurchaseStartDate(), requestDto.getPurchaseEndDate(), "구매 시작일은 종료일보다 이전이어야 합니다.");
    }

//     타입 필수 검증 (EventType이 null이면 예외)
    private void validateType(com.cMall.feedShop.event.domain.enums.EventType type) {
        if (type == null) throw new com.cMall.feedShop.event.application.exception.InvalidEventTypeException();
    }


//    필수 문자열 검증 (null 또는 빈 값이면 예외)
    private void validateRequiredString(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(message);
    }

//    필수 정수값 검증 (null이면 예외)
    private void validateRequiredInteger(Integer value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
    }

//    필수 날짜값 검증 (null이면 예외)
    private void validateRequiredDate(java.time.LocalDate value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
    }

//    날짜 순서 검증 (시작일이 종료일보다 늦으면 예외)
    private void validateDateOrder(java.time.LocalDate start, java.time.LocalDate end, String message) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException(message);
        }
    }
} 