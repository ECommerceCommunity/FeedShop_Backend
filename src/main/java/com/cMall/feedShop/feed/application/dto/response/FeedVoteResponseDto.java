package com.cMall.feedShop.feed.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedVoteResponseDto {

    private boolean voted;
    private int voteCount;
    private String message;

    public static FeedVoteResponseDto success(boolean voted, int voteCount) {
        return FeedVoteResponseDto.builder()
                .voted(voted)
                .voteCount(voteCount)
                .message(voted ? "투표가 완료되었습니다!" : "이미 투표한 피드입니다.")
                .build();
    }
}
