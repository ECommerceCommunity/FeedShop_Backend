package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사용자 팔로우 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowListResponseDto {

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 팔로우 목록 (팔로워 또는 팔로잉)
     */
    private List<UserFollowItemDto> users;

    /**
     * 페이징 정보
     */
    private PaginatedResponse<UserFollowItemDto> pagination;
}
