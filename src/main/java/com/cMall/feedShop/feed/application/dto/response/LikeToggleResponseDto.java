package com.cMall.feedShop.feed.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeToggleResponseDto {
    private boolean liked;
    private int likeCount;
}
