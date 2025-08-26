package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.user.domain.enums.RewardType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 피드 리워드 이벤트 조회 응답 DTO
 */
@Getter
@Builder
public class FeedRewardEventResponseDto {

    private Long eventId;
    private Long feedId;
    private String feedTitle;
    private Long userId;
    private String userNickname;
    private RewardType rewardType;
    private String rewardTypeDisplayName;
    private FeedRewardEvent.EventStatus eventStatus;
    private String eventStatusDisplayName;
    private Integer points;
    private String description;
    private String relatedData;
    private LocalDateTime processedAt;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * FeedRewardEvent 엔티티로부터 DTO 생성
     */
    public static FeedRewardEventResponseDto from(FeedRewardEvent event) {
        return FeedRewardEventResponseDto.builder()
                .eventId(event.getEventId())
                .feedId(event.getFeed().getId())
                .feedTitle(event.getFeed().getTitle())
                .userId(event.getUser().getId())
                .userNickname(event.getUser().getUserProfile() != null ? event.getUser().getUserProfile().getNickname() : "닉네임 없음")
                .rewardType(event.getRewardType())
                .rewardTypeDisplayName(event.getRewardType().getDisplayName())
                .eventStatus(event.getEventStatus())
                .eventStatusDisplayName(event.getEventStatus().getDisplayName())
                .points(event.getPoints())
                .description(event.getDescription())
                .relatedData(event.getRelatedData())
                .processedAt(event.getProcessedAt())
                .retryCount(event.getRetryCount())
                .errorMessage(event.getErrorMessage())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
