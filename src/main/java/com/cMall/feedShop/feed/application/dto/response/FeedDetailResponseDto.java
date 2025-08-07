package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.feed.domain.FeedType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 피드 상세 조회 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedDetailResponseDto {
    
    // 피드 기본 정보
    private Long feedId;
    private String title;
    private String content;
    private FeedType feedType;
    private String instagramId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 통계 정보
    private Integer likeCount;
    private Integer commentCount;
    private Integer participantVoteCount;
    
    // 작성자 정보 (추후 구현)
    // private UserInfoDto user;
    
    // 주문 상품 정보
    private OrderItemInfoDto orderItem;
    
    // 이벤트 정보 (이벤트 피드인 경우)
    private EventInfoDto event;
    
    // 관계 엔티티 정보
    private List<FeedHashtagDto> hashtags;
    private List<FeedImageDto> images;
    private List<FeedCommentDto> comments;
    
    // 사용자 상호작용 상태 (추후 구현)
    // private Boolean isLiked;
    // private Boolean isVoted;
    // private Boolean canVote;
    
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class OrderItemInfoDto {
        private Long orderItemId;
        private String productName;
        private Integer productSize;
        private String productImageUrl;
        private Long productId;
    }
    
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class EventInfoDto {
        private Long eventId;
        private String eventTitle;
        private String eventDescription;
        private LocalDateTime eventStartDate;
        private LocalDateTime eventEndDate;
    }
    
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FeedHashtagDto {
        private Long hashtagId;
        private String tag;
    }
    
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FeedImageDto {
        private Long imageId;
        private String imageUrl;
        private Integer sortOrder;
    }
    
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FeedCommentDto {
        private Long commentId;
        private String content;
        private LocalDateTime createdAt;
        // 작성자 정보 (추후 구현)
        // private UserInfoDto user;
    }
}
