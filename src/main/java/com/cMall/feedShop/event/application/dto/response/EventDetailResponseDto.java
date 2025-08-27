package com.cMall.feedShop.event.application.dto.response;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.enums.RewardConditionType;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventDetailResponseDto {
    private Long eventId;
    private String title;
    private String description;
    private String type;
    private String status;
    private String eventStartDate;
    private String eventEndDate;
    private String purchaseStartDate;
    private String purchaseEndDate;
    private String announcementDate;
    private String participationMethod;
    private String selectionCriteria;
    private String imageUrl;
    private String precautions;
    private Integer maxParticipants;
    private String createdBy;
    private String createdAt;
    private List<RewardDto> rewards;
    private Boolean isParticipatable; // 이벤트 참여 가능 여부

    /**
     * Event 엔티티로부터 DTO 생성
     */
    public static EventDetailResponseDto from(Event event) {
        EventDetail detail = event.getEventDetail();
        
        return EventDetailResponseDto.builder()
                .eventId(event.getId())
                .title(detail != null ? detail.getTitle() : null)
                .description(detail != null ? detail.getDescription() : null)
                .type(event.getType().name())
                .status(event.getStatus().name())
                .eventStartDate(detail != null && detail.getEventStartDate() != null ? 
                        detail.getEventStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .eventEndDate(detail != null && detail.getEventEndDate() != null ? 
                        detail.getEventEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .purchaseStartDate(detail != null && detail.getPurchaseStartDate() != null ? 
                        detail.getPurchaseStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .purchaseEndDate(detail != null && detail.getPurchaseEndDate() != null ? 
                        detail.getPurchaseEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .announcementDate(detail != null && detail.getAnnouncement() != null ? 
                        detail.getAnnouncement().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .participationMethod(detail != null ? detail.getParticipationMethod() : null)
                .selectionCriteria(detail != null ? detail.getSelectionCriteria() : null)
                .imageUrl(detail != null ? detail.getImageUrl() : null)
                .precautions(detail != null ? detail.getPrecautions() : null)
                .maxParticipants(event.getMaxParticipants())
                .createdBy(event.getCreatedUser() != null && event.getCreatedUser().getUserProfile() != null ? 
                        event.getCreatedUser().getUserProfile().getName() : null)
                .createdAt(event.getCreatedAt() != null ? 
                        event.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .rewards(event.getRewards() != null ? 
                        event.getRewards().stream()
                                .map(RewardDto::from)
                                .collect(Collectors.toList()) : null)
                .isParticipatable(event.isParticipatable())
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class RewardDto {
        private Integer rank;
        private String reward;
        @Builder.Default
        private RewardConditionType conditionType = null;
        @Builder.Default
        private String conditionDescription = "";
        @Builder.Default
        private Integer maxRecipients = 1;

        /**
         * EventReward 엔티티로부터 DTO 생성
         */
        public static RewardDto from(EventReward eventReward) {
            return RewardDto.builder()
                    .rank(eventReward.getRank())
                    .reward(eventReward.getRewardValue())
                    .conditionType(eventReward.getConditionType())
                    .conditionDescription(eventReward.getConditionDescription())
                    .maxRecipients(eventReward.getMaxRecipients())
                    .build();
        }
    }
} 