package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.feed.domain.FeedType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedCreateResponseDto {
    
    private Long feedId;
    private String title;
    private String content;
    private FeedType feedType;
    private String instagramId;
    private LocalDateTime createdAt;
    
    // 작성자 정보
    private Long userId;
    private String userNickname;
    
    // 주문 상품 정보
    private Long orderItemId;
    private String productName;
    
    // 이벤트 정보 (이벤트 참여 시에만)
    private Long eventId;
    private String eventTitle;
    
    // 해시태그 목록
    private List<String> hashtags;
    
    // 이미지 URL 목록
    private List<String> imageUrls;
} 