package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.domain.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("이벤트 검증 서비스 테스트")
class EventValidatorTest {

    private EventValidator eventValidator;

    @BeforeEach
    void setUp() {
        eventValidator = new EventValidator();
    }

    @Test
    @DisplayName("유효한 이벤트 생성 요청 검증 성공")
    void validateEventCreateRequest_ValidRequest_Success() {
        // given
        EventCreateRequestDto requestDto = createValidEventCreateRequest();

        // when & then
        eventValidator.validateEventCreateRequest(requestDto);
    }

    @Test
    @DisplayName("필수 필드 누락 시 예외 발생")
    void validateEventCreateRequest_MissingRequiredFields_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithTitle(null);

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 제목은 필수입니다.");
    }

    @Test
    @DisplayName("빈 제목 시 예외 발생")
    void validateEventCreateRequest_EmptyTitle_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithTitle("   ");

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 제목은 필수입니다.");
    }

    @Test
    @DisplayName("최대 참여자 수가 1 미만일 때 예외 발생")
    void validateEventCreateRequest_MaxParticipantsLessThanOne_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithMaxParticipants(0);

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 참여자 수는 1명 이상이어야 합니다.");
    }

    @Test
    @DisplayName("이벤트 시작일이 구매 시작일 이전인 경우 예외 발생")
    void validateEventCreateRequest_EventStartBeforePurchaseStart_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithEventStartDate(LocalDate.now().minusDays(5));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 시작일은 구매 시작일 이후나 당일이어야 합니다.");
    }

    @Test
    @DisplayName("이벤트 종료일이 구매 종료일 이전인 경우 예외 발생")
    void validateEventCreateRequest_EventEndBeforePurchaseEnd_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithEventEndDate(LocalDate.now().plusDays(5));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 종료일은 구매 종료일 이후여야 합니다.");
    }

    @Test
    @DisplayName("이벤트 종료일이 30일 이후인 경우 예외 발생")
    void validateEventCreateRequest_EventEndAfter30Days_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithEventEndDate(LocalDate.now().plusDays(31));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트 종료일은 현재 날짜로부터 30일 이내여야 합니다.");
    }

    @Test
    @DisplayName("결과 발표일이 이벤트 종료일 이전인 경우 예외 발생")
    void validateEventCreateRequest_AnnouncementBeforeEventEnd_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithAnnouncement(LocalDate.now().plusDays(15));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결과 발표일은 이벤트 종료일 이후여야 합니다.");
    }

    @Test
    @DisplayName("보상이 없을 때 예외 발생")
    void validateEventCreateRequest_NoRewards_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 1개의 보상을 입력해주세요.");
    }

    @Test
    @DisplayName("보상이 5개 초과일 때 예외 발생")
    void validateEventCreateRequest_MoreThan5Rewards_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(
                createReward("1", "1등 상품"),
                createReward("2", "2등 상품"),
                createReward("3", "3등 상품"),
                createReward("4", "4등 상품"),
                createReward("5", "5등 상품"),
                createReward("6", "6등 상품")
        ));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보상은 최대 5개까지 입력할 수 있습니다.");
    }

    @Test
    @DisplayName("보상 조건값이 null일 때 예외 발생")
    void validateEventCreateRequest_NullConditionValue_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward(null, "상품")));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 조건값을 입력해주세요.");
    }

    @Test
    @DisplayName("보상 조건값이 빈 문자열일 때 예외 발생")
    void validateEventCreateRequest_EmptyConditionValue_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward("   ", "상품")));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 조건값을 입력해주세요.");
    }

    @Test
    @DisplayName("유효하지 않은 보상 조건값일 때 예외 발생")
    void validateEventCreateRequest_InvalidConditionValue_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward("invalid", "상품")));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 조건값이 유효하지 않습니다: invalid");
    }

    @Test
    @DisplayName("등수가 1 미만일 때 예외 발생")
    void validateEventCreateRequest_RankLessThanOne_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward("0", "상품")));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 등수는 1~10 사이여야 합니다.");
    }

    @Test
    @DisplayName("등수가 10 초과일 때 예외 발생")
    void validateEventCreateRequest_RankMoreThanTen_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward("11", "상품")));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 등수는 1~10 사이여야 합니다.");
    }

    @Test
    @DisplayName("보상 내용이 null일 때 예외 발생")
    void validateEventCreateRequest_NullRewardValue_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward("1", null)));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("보상 내용이 빈 문자열일 때 예외 발생")
    void validateEventCreateRequest_EmptyRewardValue_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward("1", "   ")));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("보상 내용이 200자 초과일 때 예외 발생")
    void validateEventCreateRequest_RewardValueTooLong_ThrowsException() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(createReward("1", "a".repeat(201))));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("1번째 보상의 내용은 200자 이하여야 합니다.");
    }

    @Test
    @DisplayName("유효한 등수 조건 검증 성공")
    void validateEventCreateRequest_ValidRankCondition_Success() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(
                createReward("1", "1등 상품"),
                createReward("2", "2등 상품"),
                createReward("3", "3등 상품")
        ));

        // when & then
        eventValidator.validateEventCreateRequest(requestDto);
    }

    @Test
    @DisplayName("유효한 특별 조건 검증 성공")
    void validateEventCreateRequest_ValidSpecialConditions_Success() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(
                createReward("participation", "참여자 전원"),
                createReward("voters", "투표자 TOP"),
                createReward("views", "조회수 TOP"),
                createReward("likes", "좋아요 TOP"),
                createReward("random", "랜덤 추첨")
        ));

        // when & then
        eventValidator.validateEventCreateRequest(requestDto);
    }

    @Test
    @DisplayName("두 번째 보상에서 오류 발생 시 인덱스 표시")
    void validateEventCreateRequest_SecondRewardError_ShowsCorrectIndex() {
        // given
        EventCreateRequestDto requestDto = createEventCreateRequestWithRewards(Arrays.asList(
                createReward("1", "1등 상품"),
                createReward("invalid", "2등 상품")
        ));

        // when & then
        assertThatThrownBy(() -> eventValidator.validateEventCreateRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("2번째 보상의 조건값이 유효하지 않습니다: invalid");
    }

    // Helper methods
    private EventCreateRequestDto createValidEventCreateRequest() {
        return EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .participationMethod("구매 후 참여")
                .selectionCriteria("구매 금액 순")
                .precautions("주의사항")
                .maxParticipants(100)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(LocalDate.now().plusDays(2))
                .eventEndDate(LocalDate.now().plusDays(20))
                .announcement(LocalDate.now().plusDays(25))
                .rewards(Arrays.asList(createReward("1", "1등 상품")))
                .build();
    }

    private EventCreateRequestDto createEventCreateRequestWithTitle(String title) {
        return EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title(title)
                .description("테스트 이벤트 설명")
                .participationMethod("구매 후 참여")
                .selectionCriteria("구매 금액 순")
                .precautions("주의사항")
                .maxParticipants(100)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(LocalDate.now().plusDays(2))
                .eventEndDate(LocalDate.now().plusDays(20))
                .announcement(LocalDate.now().plusDays(25))
                .rewards(Arrays.asList(createReward("1", "1등 상품")))
                .build();
    }

    private EventCreateRequestDto createEventCreateRequestWithMaxParticipants(int maxParticipants) {
        return EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .participationMethod("구매 후 참여")
                .selectionCriteria("구매 금액 순")
                .precautions("주의사항")
                .maxParticipants(maxParticipants)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(LocalDate.now().plusDays(2))
                .eventEndDate(LocalDate.now().plusDays(20))
                .announcement(LocalDate.now().plusDays(25))
                .rewards(Arrays.asList(createReward("1", "1등 상품")))
                .build();
    }

    private EventCreateRequestDto createEventCreateRequestWithEventStartDate(LocalDate eventStartDate) {
        return EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .participationMethod("구매 후 참여")
                .selectionCriteria("구매 금액 순")
                .precautions("주의사항")
                .maxParticipants(100)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(eventStartDate)
                .eventEndDate(LocalDate.now().plusDays(20))
                .announcement(LocalDate.now().plusDays(25))
                .rewards(Arrays.asList(createReward("1", "1등 상품")))
                .build();
    }

    private EventCreateRequestDto createEventCreateRequestWithEventEndDate(LocalDate eventEndDate) {
        return EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .participationMethod("구매 후 참여")
                .selectionCriteria("구매 금액 순")
                .precautions("주의사항")
                .maxParticipants(100)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(LocalDate.now().plusDays(2))
                .eventEndDate(eventEndDate)
                .announcement(LocalDate.now().plusDays(25))
                .rewards(Arrays.asList(createReward("1", "1등 상품")))
                .build();
    }

    private EventCreateRequestDto createEventCreateRequestWithAnnouncement(LocalDate announcement) {
        return EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .participationMethod("구매 후 참여")
                .selectionCriteria("구매 금액 순")
                .precautions("주의사항")
                .maxParticipants(100)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(LocalDate.now().plusDays(2))
                .eventEndDate(LocalDate.now().plusDays(20))
                .announcement(announcement)
                .rewards(Arrays.asList(createReward("1", "1등 상품")))
                .build();
    }

    private EventCreateRequestDto createEventCreateRequestWithRewards(java.util.List<EventCreateRequestDto.EventRewardRequestDto> rewards) {
        return EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .participationMethod("구매 후 참여")
                .selectionCriteria("구매 금액 순")
                .precautions("주의사항")
                .maxParticipants(100)
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(10))
                .eventStartDate(LocalDate.now().plusDays(2))
                .eventEndDate(LocalDate.now().plusDays(20))
                .announcement(LocalDate.now().plusDays(25))
                .rewards(rewards)
                .build();
    }

    private EventCreateRequestDto.EventRewardRequestDto createReward(String conditionValue, String rewardValue) {
        return EventCreateRequestDto.EventRewardRequestDto.builder()
                .conditionValue(conditionValue)
                .rewardValue(rewardValue)
                .build();
    }
} 