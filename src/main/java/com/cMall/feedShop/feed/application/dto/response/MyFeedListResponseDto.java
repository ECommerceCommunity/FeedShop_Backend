package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.feed.domain.FeedType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 마이피드 목록 조회 응답 DTO
 * 로그인한 사용자의 피드만 조회하는 전용 DTO
 * 불변 객체로 설계하여 데이터 무결성 보장
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyFeedListResponseDto {
    
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
    
    // 작성자 정보 (본인 정보)
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
    
    // 사용자 상호작용 상태 (본인 피드이므로 기본값)
    private Boolean isLiked;
    private Boolean isVoted;
    
    /**
     * Factory 메서드: 기본 마이피드 정보로 DTO 생성
     */
    public static MyFeedListResponseDto of(Long feedId, String title, String content, FeedType feedType) {
        return MyFeedListResponseDto.builder()
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
     * Factory 메서드: 이벤트 마이피드용 DTO 생성
     */
    public static MyFeedListResponseDto eventFeed(Long feedId, String title, String content, 
                                                 Long eventId, String eventTitle) {
        return MyFeedListResponseDto.builder()
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
     * Factory 메서드: 랭킹 마이피드용 DTO 생성
     */
    public static MyFeedListResponseDto rankingFeed(Long feedId, String title, String content, 
                                                   Integer likeCount, Integer participantVoteCount) {
        return MyFeedListResponseDto.builder()
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
    public static MyFeedListResponseDto from(MyFeedListResponseDto original, Boolean isLiked) {
        return MyFeedListResponseDto.builder()
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