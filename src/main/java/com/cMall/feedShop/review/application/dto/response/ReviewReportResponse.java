package com.cMall.feedShop.review.application.dto.response;

import com.cMall.feedShop.review.domain.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "리뷰 신고 응답")
public class ReviewReportResponse {

    @Schema(description = "신고 ID", example = "1")
    private Long reportId;

    @Schema(description = "신고된 리뷰 ID", example = "123")
    private Long reviewId;

    @Schema(description = "신고자 ID", example = "456")
    private Long reporterId;

    @Schema(description = "신고 사유", example = "ABUSIVE_LANGUAGE")
    private ReportReason reason;

    @Schema(description = "신고 상세 설명", example = "욕설이 포함된 리뷰입니다.")
    private String description;

    @Schema(description = "관리자 처리 여부", example = "false")
    private Boolean isProcessed;

    @Schema(description = "신고 일시", example = "2025-08-28T10:30:00")
    private LocalDateTime createdAt;
}