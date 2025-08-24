package com.cMall.feedShop.feed.application.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedVoteResponseDto {
    
    private Long voteId;
    private Long feedId;
    private Long userId;
    private Long eventId;
    private LocalDateTime votedAt;
}
