// 이벤트 생성 요청 DTO
package com.cMall.feedShop.event.application.dto.request;

import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.enums.RewardConditionType;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

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
    
    // 이벤트 보상 정보 (FormData 배열 파라미터로 전송)
    @Builder.Default
    private List<EventRewardRequestDto> rewards = new ArrayList<>();
    
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class EventRewardRequestDto {
        @NotBlank(message = "보상 조건은 필수입니다.")
        private String conditionValue; // "1", "2", "3", "participation", "voters", "views", "likes", "random"
        
        @NotBlank(message = "보상 내용은 필수입니다.")
        @Size(max = 200, message = "보상 내용은 200자 이하여야 합니다.")
        private String rewardValue;    // 보상 내용
        
        /**
         * 조건값 유효성 검증 (비즈니스 로직)
         * 
         * @return 유효하면 true, 그렇지 않으면 false
         */
        public boolean isValidConditionValue() {
            RewardConditionType conditionType = RewardConditionType.fromString(conditionValue);
            if (conditionType == null) {
                return false;
            }
            
            // 등수 조건인 경우 숫자 범위 검증
            if (conditionType.isRank()) {
                try {
                    int rank = Integer.parseInt(conditionValue);
                    return rank >= 1 && rank <= 10;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * 조건값 비즈니스 로직 검증 및 예외 발생
         * 
         * @throws IllegalArgumentException 유효하지 않은 경우
         */
        public void validateConditionValue() {
            RewardConditionType conditionType = RewardConditionType.fromString(conditionValue);
            if (conditionType == null) {
                throw new IllegalArgumentException("유효하지 않은 보상 조건값입니다: " + conditionValue);
            }
            
            // 등수 조건인 경우 숫자 범위 검증
            if (conditionType.isRank()) {
                try {
                    int rank = Integer.parseInt(conditionValue);
                    if (rank < 1 || rank > 10) {
                        throw new IllegalArgumentException("보상 등수는 1~10 사이여야 합니다: " + conditionValue);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("보상 등수가 유효하지 않습니다: " + conditionValue);
                }
            }
        }
        
        /**
         * 전체 보상 정보 비즈니스 로직 검증
         * 
         * @throws IllegalArgumentException 유효하지 않은 경우
         */
        public void validate() {
            validateConditionValue();
            // rewardValue는 @NotBlank, @Size로 이미 검증됨
        }
    }
} 