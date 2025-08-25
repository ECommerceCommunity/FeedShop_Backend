package com.cMall.feedShop.user.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팔로우/언팔로우 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowToggleRequestDto {

    /**
     * 팔로우할 사용자 ID
     */
    @NotNull(message = "팔로우할 사용자 ID는 필수입니다.")
    private Long followingUserId;
}
