package com.cMall.feedShop.feed.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedVoteRequestDto {

    @NotNull(message = "피드 ID는 필수입니다.")
    private Long feedId;

    public FeedVoteRequestDto(Long feedId) {
        this.feedId = feedId;
    }
}
