package com.cMall.feedShop.feed.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentCreateRequestDto {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글 내용은 1000자를 초과할 수 없습니다.")
    private String content;

    @Builder
    public CommentCreateRequestDto(String content) {
        this.content = content;
    }
}
