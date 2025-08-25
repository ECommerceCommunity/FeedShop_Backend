package com.cMall.feedShop.event.application.dto.response;

import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.EventResultDetail;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 이벤트 결과 응답 DTO
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventResultResponseDto {
    
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String resultType;
    private LocalDateTime announcedAt;
    private Integer totalParticipants;
    private Long totalVotes;
    private List<EventResultDetailResponseDto> resultDetails;
    
    @Builder
    public EventResultResponseDto(Long id, Long eventId, String eventTitle, String resultType, 
                                 LocalDateTime announcedAt, Integer totalParticipants, Long totalVotes, 
                                 List<EventResultDetailResponseDto> resultDetails) {
        this.id = id;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.resultType = resultType;
        this.announcedAt = announcedAt;
        this.totalParticipants = totalParticipants;
        this.totalVotes = totalVotes;
        this.resultDetails = resultDetails;
    }
    
    /**
     * EventResult 엔터티로부터 DTO 생성
     */
    public static EventResultResponseDto from(EventResult eventResult) {
        return EventResultResponseDto.builder()
                .id(eventResult.getId())
                .eventId(eventResult.getEvent().getId())
                .eventTitle(eventResult.getEvent().getEventDetail() != null ? 
                           eventResult.getEvent().getEventDetail().getTitle() : "")
                .resultType(eventResult.getResultType().name())
                .announcedAt(eventResult.getAnnouncedAt())
                .totalParticipants(eventResult.getTotalParticipants())
                .totalVotes(eventResult.getTotalVotes())
                .resultDetails(eventResult.getResultDetails().stream()
                        .map(EventResultDetailResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
    
    /**
     * 이벤트 결과 상세 정보 응답 DTO
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class EventResultDetailResponseDto {
        
        private Long id;
        private Long userId;
        private String userName;
        private Long feedId;
        private String feedTitle;
        private Integer rankPosition;
        private Long voteCount;
        private Integer pointsEarned;
        private Integer badgePointsEarned;
        private String couponCode;
        private String couponDescription;
        private Boolean rewardProcessed;
        private LocalDateTime rewardProcessedAt;
        
        @Builder
        public EventResultDetailResponseDto(Long id, Long userId, String userName, Long feedId, String feedTitle,
                                          Integer rankPosition, Long voteCount, Integer pointsEarned, 
                                          Integer badgePointsEarned, String couponCode, String couponDescription,
                                          Boolean rewardProcessed, LocalDateTime rewardProcessedAt) {
            this.id = id;
            this.userId = userId;
            this.userName = userName;
            this.feedId = feedId;
            this.feedTitle = feedTitle;
            this.rankPosition = rankPosition;
            this.voteCount = voteCount;
            this.pointsEarned = pointsEarned;
            this.badgePointsEarned = badgePointsEarned;
            this.couponCode = couponCode;
            this.couponDescription = couponDescription;
            this.rewardProcessed = rewardProcessed;
            this.rewardProcessedAt = rewardProcessedAt;
        }
        
        /**
         * EventResultDetail 엔터티로부터 DTO 생성
         */
        public static EventResultDetailResponseDto from(EventResultDetail detail) {
            return EventResultDetailResponseDto.builder()
                    .id(detail.getId())
                    .userId(detail.getUser().getId())
                    .userName(detail.getUser().getUserProfile() != null ? 
                             detail.getUser().getUserProfile().getName() : "")
                    .feedId(null) // EventResultDetail에는 feedId 필드가 없음
                    .feedTitle(null) // EventResultDetail에는 feedTitle 필드가 없음
                    .rankPosition(detail.getRankPosition())
                    .voteCount(detail.getVoteCount())
                    .pointsEarned(detail.getPointsEarned())
                    .badgePointsEarned(detail.getBadgePointsEarned())
                    .couponCode(detail.getCouponCode())
                    .couponDescription(detail.getCouponDescription())
                    .rewardProcessed(detail.getRewardProcessed())
                    .rewardProcessedAt(detail.getRewardProcessedAt())
                    .build();
        }
    }
}
