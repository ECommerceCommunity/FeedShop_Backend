package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.application.dto.response.EventDetailResponseDto;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.application.dto.response.EventListResponseDto;
import com.cMall.feedShop.common.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventMapper {

    private final EventStatusService eventStatusService;

    /**
     * Entity → DTO 변환 (간단 버전)
     */
    public EventSummaryDto toSummaryDto(Event event) {
        EventDetail detail = event.getEventDetail();
        
        return EventSummaryDto.builder()
                .eventId(event.getId())
                .title(getSafeString(detail, EventDetail::getTitle))
                .type(getEventType(event))
                .status(getRealTimeEventStatus(event)) // 실시간 상태 계산
                .eventStartDate(getSafeLocalDateString(detail, EventDetail::getEventStartDate))
                .eventEndDate(getSafeLocalDateString(detail, EventDetail::getEventEndDate))
                .imageUrl(getSafeString(detail, EventDetail::getImageUrl))
                .maxParticipants(event.getMaxParticipants())
                .description(getSafeString(detail, EventDetail::getDescription))
                .rewards(mapRewards(event))
                .participationMethod(getSafeString(detail, EventDetail::getParticipationMethod))
                .selectionCriteria(getSafeString(detail, EventDetail::getSelectionCriteria))
                .precautions(getSafeString(detail, EventDetail::getPrecautions))
                .createdBy("")
                .createdAt(event.getCreatedBy() != null ? event.getCreatedBy().toString() : null)
                .purchasePeriod(createPurchasePeriod(detail))
                .votePeriod(createVotePeriod(detail))
                .announcementDate(getSafeLocalDateString(detail, EventDetail::getAnnouncement))
                .isParticipatable(eventStatusService.isEventParticipatable(event)) // 참여 가능 여부 추가
                .build();
    }

    /**
     * Event를 EventDetailResponseDto로 변환
     */
    public EventDetailResponseDto toDetailDto(Event event) {
        Boolean isParticipatable = eventStatusService.isEventParticipatable(event);
        return EventDetailResponseDto.from(event, isParticipatable);
    }

    private List<EventSummaryDto.Reward> mapRewards(Event event) {
        return event.getRewards() != null ? event.getRewards().stream()
                .map(r -> EventSummaryDto.Reward.builder()
                        .rank(r.isRankCondition() ? r.getRank() : null)
                        .reward(r.getRewardValue())
                        .conditionType(r.getConditionType())
                        .conditionDescription(r.getConditionDescription())
                        .build())
                .toList() : Collections.emptyList();
    }

    private List<EventDetailResponseDto.RewardDto> mapDetailRewards(Event event) {
        return event.getRewards() != null ? event.getRewards().stream()
                .map(this::toRewardDto)
                .toList() : Collections.emptyList();
    }

    private EventDetailResponseDto.RewardDto toRewardDto(EventReward reward) {
        return EventDetailResponseDto.RewardDto.builder()
                .rank(reward.isRankCondition() ? reward.getRank() : null)
                .reward(reward.getRewardValue())
                .conditionType(reward.getConditionType())
                .conditionDescription(reward.getConditionDescription())
                .maxRecipients(reward.getMaxRecipients())
                .build();
    }

    private String createPurchasePeriod(EventDetail detail) {
        return createDateRangeString(detail, EventDetail::getPurchaseStartDate, EventDetail::getPurchaseEndDate);
    }

    private String createVotePeriod(EventDetail detail) {
        return createDateRangeString(detail, EventDetail::getEventStartDate, EventDetail::getEventEndDate);
    }

    private String createDateRangeString(EventDetail detail, 
                                       Function<EventDetail, java.time.LocalDate> startGetter,
                                       Function<EventDetail, java.time.LocalDate> endGetter) {
        if (detail == null) return "";
        
        var startDate = startGetter.apply(detail);
        var endDate = endGetter.apply(detail);
        
        return (startDate != null && endDate != null) 
                ? startDate + " ~ " + endDate 
                : "";
    }

    private String getSafeString(EventDetail detail, Function<EventDetail, String> getter) {
        return detail != null ? getter.apply(detail) : "";
    }

    private String getSafeLocalDateString(EventDetail detail, Function<EventDetail, java.time.LocalDate> getter) {
        if (detail == null) return "";
        var date = getter.apply(detail);
        return date != null ? date.toString() : "";
    }

    private String getEventType(Event event) {
        return event.getType() != null ? event.getType().name().toLowerCase() : null;
    }

    /**
     * 실시간으로 계산된 상태 반환
     */
    private String getRealTimeEventStatus(Event event) {
        if (event.getStatus() == null) {
            return null;
        }
        
        // EventStatusService를 사용하여 실시간 상태 계산
        var calculatedStatus = eventStatusService.calculateEventStatus(event, TimeUtil.nowDate());
        return calculatedStatus != null ? calculatedStatus.name().toLowerCase() : null;
    }
} 