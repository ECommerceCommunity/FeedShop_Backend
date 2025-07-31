package com.cMall.feedShop.event.application.dto.response;

import com.cMall.feedShop.event.domain.enums.RewardConditionType;
import lombok.*;
import java.util.List;

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

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class RewardDto {
        private Integer rank;
        private String reward;
        private RewardConditionType conditionType;
        private String conditionDescription;
        private Integer maxRecipients;
    }
} 