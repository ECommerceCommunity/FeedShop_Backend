// 이벤트 생성 요청 DTO
package com.cMall.feedShop.event.application.dto.request;

import com.cMall.feedShop.event.domain.enums.EventType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
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
    
    // 이벤트 보상 정보 (구조화된 객체 리스트로 전송)
    private List<EventRewardRequestDto> rewards;
    
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class EventRewardRequestDto {
        private Integer conditionValue; // 순위 (1, 2, 3...)
        private String rewardValue;     // 보상 내용
    }
} 