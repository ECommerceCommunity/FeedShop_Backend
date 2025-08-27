package com.cMall.feedShop.review.application.dto.response;

import com.cMall.feedShop.review.domain.enums.ReportReason;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "신고된 리뷰 정보 (관리자용)")
public class ReportedReviewResponse {

    @Schema(description = "리뷰 ID")
    private Long reviewId;

    @Schema(description = "리뷰 제목")
    private String title;

    @Schema(description = "리뷰 내용")
    private String content;

    @Schema(description = "작성자 ID")
    private Long writerId;

    @Schema(description = "작성자 이름")
    private String writerName;

    @Schema(description = "상품 ID")
    private Long productId;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "리뷰 상태")
    private ReviewStatus status;

    @Schema(description = "총 신고 수")
    private Long totalReportCount;

    @Schema(description = "처리되지 않은 신고 수")
    private Long unprocessedReportCount;

    @Schema(description = "신고 목록")
    private List<ReportInfo> reports;

    @Schema(description = "리뷰 작성일")
    private LocalDateTime reviewCreatedAt;

    @Getter
    @Builder
    public static class ReportInfo {
        @Schema(description = "신고 ID")
        private Long reportId;

        @Schema(description = "신고자 ID")
        private Long reporterId;

        @Schema(description = "신고자 이름")
        private String reporterName;

        @Schema(description = "신고 사유")
        private ReportReason reason;

        @Schema(description = "신고 상세 설명")
        private String description;

        @Schema(description = "처리 여부")
        private Boolean isProcessed;

        @Schema(description = "신고 일시")
        private LocalDateTime createdAt;
    }
}