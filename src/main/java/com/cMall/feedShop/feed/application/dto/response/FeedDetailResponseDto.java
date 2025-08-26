package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.feed.domain.enums.FeedType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 피드 상세 조회 응답 DTO
 * 목록 DTO보다 더 상세한 정보를 포함
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
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
    
    // 작성자 상세 정보
    private Long userId;
    private String userNickname;
    private String userProfileImg;
    private Integer userLevel;
    private String userGender;
    private Integer userHeight;
    
    // 주문 상품 상세 정보
    private Long orderItemId;
    private String productName;
    private Integer productSize;
    private String productImageUrl;
    private Long productId;
    
    // 이벤트 상세 정보 (이벤트 피드인 경우)
    private Long eventId;
    private String eventTitle;
    private String eventDescription;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;
    
    // 관계 엔티티 상세 정보
    private List<FeedHashtagDto> hashtags;
    private List<FeedImageDto> images;
    private List<FeedCommentDto> comments;
    
    // 사용자 상호작용 상태
    private Boolean isLiked;
    private Boolean isVoted;
    private Boolean canVote; // 이벤트 투표 가능 여부
    
    /**
     * Factory 메서드: 기본 피드 상세 정보로 DTO 생성
     */
    public static FeedDetailResponseDto of(Long feedId, String title, String content, FeedType feedType) {
        return FeedDetailResponseDto.builder()
                .feedId(feedId)
                .title(title)
                .content(content)
                .feedType(feedType)
                .likeCount(0)
                .commentCount(0)
                .participantVoteCount(0)
                .isLiked(false)
                .isVoted(false)
                .canVote(false)
                .build();
    }
    
    /**
     * Factory 메서드: 이벤트 피드 상세용 DTO 생성
     */
    public static FeedDetailResponseDto eventFeedDetail(Long feedId, String title, String content, 
                                                     Long eventId, String eventTitle, String eventDescription) {
        return FeedDetailResponseDto.builder()
                .feedId(feedId)
                .title(title)
                .content(content)
                .feedType(FeedType.EVENT)
                .eventId(eventId)
                .eventTitle(eventTitle)
                .eventDescription(eventDescription)
                .likeCount(0)
                .commentCount(0)
                .participantVoteCount(0)
                .isLiked(false)
                .isVoted(false)
                .canVote(true) // 이벤트 피드는 투표 가능
                .build();
    }
    
    /**
     * Factory 메서드: 랭킹 피드 상세용 DTO 생성
     */
    public static FeedDetailResponseDto rankingFeedDetail(Long feedId, String title, String content, 
                                                        Integer likeCount, Integer participantVoteCount) {
        return FeedDetailResponseDto.builder()
                .feedId(feedId)
                .title(title)
                .content(content)
                .feedType(FeedType.RANKING)
                .likeCount(likeCount)
                .participantVoteCount(participantVoteCount)
                .commentCount(0)
                .isLiked(false)
                .isVoted(false)
                .canVote(false) // 랭킹 피드는 투표 불가
                .build();
    }
    
    /**
     * 내부 DTO 클래스: 해시태그 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FeedHashtagDto {
        private Long tagId;
        private String tag;
    }
    
    /**
     * 내부 DTO 클래스: 이미지 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FeedImageDto {
        private Long imageId;
        private String imageUrl;
        private Integer sortOrder;
        private LocalDateTime uploadedAt;
    }
    
    /**
     * 내부 DTO 클래스: 댓글 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FeedCommentDto {
        private Long commentId;
        private String content;
        private LocalDateTime createdAt;
        private Long userId;
        private String userNickname;
        private String userProfileImg;
    }
} 