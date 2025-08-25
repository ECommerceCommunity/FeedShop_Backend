package com.cMall.feedShop.feed.application.dto.response;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentListResponseDto {

    private PaginatedResponse<CommentResponseDto> pagination;
    private long totalComments;

    @Builder
    public CommentListResponseDto(PaginatedResponse<CommentResponseDto> pagination, long totalComments) {
        this.pagination = pagination;
        this.totalComments = totalComments;
    }
}
