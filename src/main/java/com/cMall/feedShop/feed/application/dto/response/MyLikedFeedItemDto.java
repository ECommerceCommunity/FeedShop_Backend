package com.cMall.feedShop.feed.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 좋아요한 피드 개별 아이템 응답 DTO
 * 사용자가 좋아요를 누른 피드의 기본 정보와 좋아요 시간을 제공
 */
@Getter
@Builder
public class MyLikedFeedItemDto {
    
    /**
     * 피드 ID
     */
    private final Long feedId;
    
    /**
     * 피드 제목
     */
    private final String title;
    
    /**
     * 피드 내용 (일부만 표시)
     */
    private final String content;
    
    /**
     * 피드 타입 (DAILY, EVENT, RANKING)
     */
    private final String feedType;
    
    /**
     * 대표 이미지 URL
     */
    private final String imageUrl;
    
    /**
     * 좋아요를 누른 시간
     */
    private final LocalDateTime likedAt;
    
    /**
     * 현재 좋아요 수
     */
    private final int likeCount;
    
    /**
     * 현재 댓글 수
     */
    private final int commentCount;
    
    /**
     * 피드 작성자 닉네임
     */
    private final String authorNickname;
    
    /**
     * 피드 작성자 프로필 이미지
     */
    private final String authorProfileImage;
}
