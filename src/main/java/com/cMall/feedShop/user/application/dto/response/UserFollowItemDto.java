package com.cMall.feedShop.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팔로우 목록의 개별 사용자 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowItemDto {

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 닉네임
     */
    private String nickname;

    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;

    /**
     * 팔로우 시작 시간
     */
    private LocalDateTime followedAt;
}
