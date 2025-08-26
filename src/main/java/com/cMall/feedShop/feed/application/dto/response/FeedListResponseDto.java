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
 * 피드 목록 조회 응답 DTO
 * 불변 객체로 설계하여 데이터 무결성 보장
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedListResponseDto {
    
    // 피드 기본 정보
    private Long feedId;
    private String title;
    private String content;
    private FeedType feedType;
    private String instagramId;
    private LocalDateTime createdAt;
    
    // 통계 정보
    private Integer likeCount;
    private Integer commentCount;
    private Integer participantVoteCount;
    
    // 작성자 정보
    private Long userId;
    private String userNickname;
    private String userProfileImg;
    private Integer userLevel;
    
    // 주문 상품 정보
    private Long orderItemId;
    private String productName;
    private Integer productSize;
    
    // 이벤트 정보 (이벤트 피드인 경우)
    private Long eventId;
    private String eventTitle;
    
    // 관계 엔티티 정보
    private List<String> hashtags;
    private List<String> imageUrls;
    
    // 사용자 상호작용 상태
    // false: 좋아요하지 않음, true: 좋아요함
    // 서비스 레이어에서 실제 사용자 상태로 업데이트됨
    private Boolean isLiked;
    
    // false: 투표하지 않음, true: 투표함
    // 서비스 레이어에서 실제 사용자 상태로 업데이트됨
    private Boolean isVoted;
    
    /**
     * Factory 메서드: 기본 피드 정보로 DTO 생성
     */
    public static FeedListResponseDto of(Long feedId, String title, String content, FeedType feedType) {
        return FeedListResponseDto.builder()
                .feedId(feedId)
                .title(title)
                .content(content)
                .feedType(feedType)
                .likeCount(0)
                .commentCount(0)
                .participantVoteCount(0)
                .isLiked(false)
                .isVoted(false)
                .build();
    }
    
    /**
     * Factory 메서드: 이벤트 피드용 DTO 생성
     */
    public static FeedListResponseDto eventFeed(Long feedId, String title, String content, 
                                              Long eventId, String eventTitle) {
        return FeedListResponseDto.builder()
                .feedId(feedId)
                .title(title)
                .content(content)
                .feedType(FeedType.EVENT)
                .eventId(eventId)
                .eventTitle(eventTitle)
                .likeCount(0)
                .commentCount(0)
                .participantVoteCount(0)
                .isLiked(false)
                .isVoted(false)
                .build();
    }
    
    /**
     * Factory 메서드: 랭킹 피드용 DTO 생성
     */
    public static FeedListResponseDto rankingFeed(Long feedId, String title, String content, 
                                                Integer likeCount, Integer participantVoteCount) {
        return FeedListResponseDto.builder()
                .feedId(feedId)
                .title(title)
                .content(content)
                .feedType(FeedType.RANKING)
                .likeCount(likeCount)
                .participantVoteCount(participantVoteCount)
                .commentCount(0)
                .isLiked(false)
                .isVoted(false)
                .build();
    }
    
    /**
     * Factory 메서드: 기존 DTO에서 isLiked 상태만 업데이트
     * 서비스 레이어에서 사용자별 좋아요 상태를 설정할 때 사용
     */
    public static FeedListResponseDto from(FeedListResponseDto original, Boolean isLiked) {
        return FeedListResponseDto.builder()
                .feedId(original.getFeedId())
                .title(original.getTitle())
                .content(original.getContent())
                .feedType(original.getFeedType())
                .instagramId(original.getInstagramId())
                .createdAt(original.getCreatedAt())
                .likeCount(original.getLikeCount())
                .commentCount(original.getCommentCount())
                .participantVoteCount(original.getParticipantVoteCount())
                .userId(original.getUserId())
                .userNickname(original.getUserNickname())
                .userProfileImg(original.getUserProfileImg())
                .userLevel(original.getUserLevel())
                .orderItemId(original.getOrderItemId())
                .productName(original.getProductName())
                .productSize(original.getProductSize())
                .eventId(original.getEventId())
                .eventTitle(original.getEventTitle())
                .hashtags(original.getHashtags())
                .imageUrls(original.getImageUrls())
                .isLiked(isLiked)
                .isVoted(original.getIsVoted())
                .build();
    }
} 