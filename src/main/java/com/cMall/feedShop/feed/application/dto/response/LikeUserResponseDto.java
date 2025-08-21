package com.cMall.feedShop.feed.application.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 좋아요 사용자 정보 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LikeUserResponseDto {
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 사용자 닉네임
     */
    private String nickname;
    
    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;
    
    /**
     * 사용자 레벨
     */
    private Integer level;
    
    /**
     * 좋아요 누른 시간
     */
    private LocalDateTime likedAt;
}
