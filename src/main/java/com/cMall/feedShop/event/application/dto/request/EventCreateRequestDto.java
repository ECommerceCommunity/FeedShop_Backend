// 이벤트 생성 요청 DTO
package com.cMall.feedShop.event.application.dto.request;

import com.cMall.feedShop.event.domain.enums.EventType;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventCreateRequestDto {
    
    // 이벤트 기본 정보
    private EventType type;
    private Integer maxParticipants;
    
    // 이벤트 상세 정보
    private String title;
    private String description;
    private String imageUrl;
    private String participationMethod;
    private String selectionCriteria;
    private String precautions;
    
    // 이벤트 날짜 정보
    private LocalDate purchaseStartDate;
    private LocalDate purchaseEndDate;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalDate announcement;
    
    // 이벤트 보상 정보 (프론트엔드에서 문자열로 전송)
    private String rewards;

    // Setter 메서드들 추가
    public void setType(EventType type) {
        this.type = type;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setParticipationMethod(String participationMethod) {
        this.participationMethod = participationMethod;
    }

    public void setSelectionCriteria(String selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions;
    }

    public void setPurchaseStartDate(LocalDate purchaseStartDate) {
        this.purchaseStartDate = purchaseStartDate;
    }

    public void setPurchaseEndDate(LocalDate purchaseEndDate) {
        this.purchaseEndDate = purchaseEndDate;
    }

    public void setEventStartDate(LocalDate eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public void setEventEndDate(LocalDate eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public void setAnnouncement(LocalDate announcement) {
        this.announcement = announcement;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }
    
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class EventRewardRequestDto {
        private Integer conditionValue; // 순위 (1, 2, 3...)
        private String rewardValue;     // 보상 내용
    }
} 