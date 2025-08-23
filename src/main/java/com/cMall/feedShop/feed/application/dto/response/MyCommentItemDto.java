package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.feed.domain.Comment;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyCommentItemDto {
    
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    
    // 피드 정보
    private Long feedId;
    private String feedTitle;
    private FeedType feedType;
    private String feedImageUrl;
    
    // 피드 작성자 정보
    private String authorNickname;
    private String authorProfileImage;
    
    public static MyCommentItemDto from(Comment comment) {
        Feed feed = comment.getFeed();
        
        return MyCommentItemDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .feedId(feed.getId())
                .feedTitle(feed.getTitle())
                .feedType(feed.getFeedType())
                .feedImageUrl(getFirstImageUrl(feed))
                .authorNickname(feed.getUser().getUserProfile() != null ? feed.getUser().getUserProfile().getNickname() : null)
                .authorProfileImage(feed.getUser().getUserProfile() != null ? feed.getUser().getUserProfile().getProfileImageUrl() : null)
                .build();
    }
    
    private static String getFirstImageUrl(Feed feed) {
        // TODO: 피드의 첫 번째 이미지 URL을 가져오는 로직 구현
        // 현재는 Feed 엔티티에 이미지 관련 필드가 없으므로 null 반환
        return null;
    }
}
