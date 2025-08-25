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

    @Schema(description = "신고 ID")
    private Long reportId;

    @Schema(description = "리뷰 ID")
    private Long reviewId;

    @Schema(description = "신고자 ID")
    private Long reporterId;

    @Schema(description = "신고 사유")
    private ReportReason reason;

    @Schema(description = "신고 상세 설명")
    private String description;

    @Schema(description = "처리 여부")
    private Boolean isProcessed;

    @Schema(description = "신고 일시")
    private LocalDateTime createdAt;
}