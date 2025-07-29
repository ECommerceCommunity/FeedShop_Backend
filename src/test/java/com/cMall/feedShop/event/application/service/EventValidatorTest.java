package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.exception.InvalidEventTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EventValidator 테스트")
class EventValidatorTest {

    private EventValidator eventValidator;
    private EventCreateRequestDto validRequestDto;

    @BeforeEach
    void setUp() {
        eventValidator = new EventValidator();
        
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .eventStartDate(LocalDate.now().plusDays(7))
                .eventEndDate(LocalDate.now().plusDays(14))
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(5))
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
    }

    @Test
    @DisplayName("유효한 요청 검증 성공")
    void validateEventCreateRequest_Success() {
        // When & Then
        eventValidator.validateEventCreateRequest(validRequestDto);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("이벤트 타입이 null인 경우 예외 발생")
    void validateEventCreateRequest_NullType() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();

        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(InvalidEventTypeException.class);
    }

    @Test
    @DisplayName("제목이 null인 경우 예외 발생")
    void validateEventCreateRequest_NullTitle() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .build();

        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 제목은 필수입니다.");
    }

    @Test
    @DisplayName("제목이 빈 문자열인 경우 예외 발생")
    void validateEventCreateRequest_EmptyTitle() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .build();

        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 제목은 필수입니다.");
    }

    @Test
    @DisplayName("설명이 null인 경우 예외 발생")
    void validateEventCreateRequest_NullDescription() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .maxParticipants(10)
                .build();

        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 설명은 필수입니다.");
    }

    @Test
    @DisplayName("설명이 빈 문자열인 경우 예외 발생")
    void validateEventCreateRequest_EmptyDescription() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("")
                .maxParticipants(10)
                .build();

        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 설명은 필수입니다.");
    }

    @Test
    @DisplayName("이벤트 시작일이 종료일보다 늦은 경우 예외 발생")
    void validateEventCreateRequest_InvalidEventDateRange() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .eventStartDate(LocalDate.now().plusDays(7))
                .eventEndDate(LocalDate.now())
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 시작일은 종료일보다 이전이어야 합니다.");
    }

    @Test
    @DisplayName("구매 시작일이 종료일보다 늦은 경우 예외 발생")
    void validateEventCreateRequest_InvalidPurchaseDateRange() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .eventStartDate(LocalDate.now())
                .eventEndDate(LocalDate.now().plusDays(1))
                .purchaseStartDate(LocalDate.now().plusDays(5))
                .purchaseEndDate(LocalDate.now())
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구매 시작일은 종료일보다 이전이어야 합니다.");
    }

    @Test
    @DisplayName("maxParticipants가 null인 경우 예외 발생")
    void validateEventCreateRequest_NullMaxParticipants() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(null)
                .eventStartDate(LocalDate.now())
                .eventEndDate(LocalDate.now().plusDays(1))
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 참여자 수는 필수입니다.");
    }

    @Test
    @DisplayName("eventStartDate가 null인 경우 예외 발생")
    void validateEventCreateRequest_NullEventStartDate() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .eventStartDate(null)
                .eventEndDate(LocalDate.now().plusDays(1))
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 시작일은 필수입니다.");
    }

    @Test
    @DisplayName("eventEndDate가 null인 경우 예외 발생")
    void validateEventCreateRequest_NullEventEndDate() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .eventStartDate(LocalDate.now())
                .eventEndDate(null)
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 종료일은 필수입니다.");
    }

    @Test
    @DisplayName("rewards가 null이거나 빈 값인 경우 예외 발생")
    void validateEventCreateRequest_NullOrEmptyRewards() {
        // rewards가 null
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .eventStartDate(LocalDate.now())
                .eventEndDate(LocalDate.now().plusDays(1))
                .rewards(null)
                .build();
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 보상 정보는 필수입니다.");
        // rewards가 빈 값
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .eventStartDate(LocalDate.now())
                .eventEndDate(LocalDate.now().plusDays(1))
                .rewards(List.of())
                .build();
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 보상 정보는 필수입니다.");
    }

    @Test
    @DisplayName("이벤트 종료일이 구매 종료일 이전인 경우 예외 발생")
    void validateEventCreateRequest_EventEndBeforePurchaseEnd() {
        // Given: purchaseStart < eventStart < purchaseEnd > eventEnd (실패 유도)
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(5))
                .eventStartDate(LocalDate.now().plusDays(1)) // purchaseStart < eventStart
                .eventEndDate(LocalDate.now().plusDays(4))   // eventEnd < purchaseEnd (실패 유도)
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 종료일은 구매 종료일 이후여야 합니다.");
    }

    @Test
    @DisplayName("이벤트 시작일이 구매 시작일 이전인 경우 예외 발생")
    void validateEventCreateRequest_EventStartAfterPurchaseEnd() {
        // Given: eventStart < purchaseStart (실패 유도)
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .purchaseStartDate(LocalDate.now().plusDays(5))
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(LocalDate.now().plusDays(3)) // eventStart < purchaseStart (실패 유도)
                .eventEndDate(LocalDate.now().plusDays(12))
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(validRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 시작일은 구매 시작일 이후나 당일이어야 합니다.");
    }

    @Test
    @DisplayName("유효한 날짜 범위 검증 성공")
    void validateEventCreateRequest_ValidDateRange() {
        // Given: purchaseEnd < eventStart < eventEnd > purchaseEnd
        validRequestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(10)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(5))
                .eventStartDate(LocalDate.now().plusDays(6)) // purchaseEnd < eventStart
                .eventEndDate(LocalDate.now().plusDays(10))  // eventEnd > purchaseEnd
                .rewards(List.of(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue(1)
                        .rewardValue("상품")
                        .build()
                ))
                .build();
        // When & Then: 예외가 발생하지 않아야 함
        eventValidator.validateEventCreateRequest(validRequestDto);
    }
} 