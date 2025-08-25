package com.cMall.feedShop.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팔로우/언팔로우 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowToggleResponseDto {

    /**
     * 팔로우 상태 (true: 팔로우됨, false: 언팔로우됨)
     */
    private boolean following;

    /**
     * 팔로워 수
     */
    private long followerCount;

    /**
     * 팔로잉 수
     */
    private long followingCount;

    /**
     * 메시지
     */
    private String message;
}
