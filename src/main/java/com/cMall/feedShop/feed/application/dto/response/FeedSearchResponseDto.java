package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.feed.domain.enums.FeedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 피드 검색 응답 DTO (프론트엔드 최적화)
 * - userLevel, orderItemId, productSize 필드 제거
 * - 검색 결과에 최적화된 응답 구조
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedSearchResponseDto {
    
    // 기본 피드 정보
    private Long feedId;
    private String title;
    private String content;
    private FeedType feedType;
    private String instagramId;
    private LocalDateTime createdAt;
    
    // 카운트 정보
    private Integer likeCount;
    private Integer commentCount;
    private Integer participantVoteCount;
    
    // 사용자 정보
    private Long userId;
    private String userNickname;
    private String userProfileImg;
    
    // 상품 정보 (필요한 필드만)
    private String productName;
    
    // 이벤트 정보
    private Long eventId;
    private String eventTitle;
    
    // 기타 정보
    private List<String> hashtags;
    private List<String> imageUrls;
    
    // 사용자 상호작용 상태
    private Boolean isLiked;
    private Boolean isVoted;
}
