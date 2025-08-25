package com.cMall.feedShop.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 팔로우 수 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowCountResponseDto {

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 팔로워 수
     */
    private long followerCount;

    /**
     * 팔로잉 수
     */
    private long followingCount;
}
