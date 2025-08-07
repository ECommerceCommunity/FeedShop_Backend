// 이벤트 목록의 단일 이벤트 요약 응답 DTO
package com.cMall.feedShop.event.application.dto.response;

import com.cMall.feedShop.event.domain.enums.RewardConditionType;
import lombok.*;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventSummaryDto {
    private Long eventId;
    private String title;
    private String type;
    private String status;
    private String eventStartDate;
    private String eventEndDate;
    private String imageUrl;
    private Integer maxParticipants;
    private String description;
    private List<Reward> rewards;
    private String participationMethod;
    private String selectionCriteria;
    private String precautions;
    private String createdBy;
    private String createdAt;
    private String purchasePeriod;
    private String votePeriod;
    private String announcementDate;
    private Boolean isParticipatable; // 이벤트 참여 가능 여부

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class Reward {
        private Integer rank;
        private String reward;
        @Builder.Default
        private RewardConditionType conditionType = null;
        @Builder.Default
        private String conditionDescription = "";
    }
} 