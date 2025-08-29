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

    @Schema(description = "신고된 리뷰 ID", example = "123")
    private Long reviewId;

    @Schema(description = "리뷰 제목", example = "정말 좋은 상품이에요!")
    private String title;

    @Schema(description = "리뷰 내용 (일부만 표시됨)", example = "좋은 상품이지만 배송이 너무 느렸습니다...")
    private String content;

    @Schema(description = "리뷰 작성자 ID", example = "456")
    private Long writerId;

    @Schema(description = "리뷰 작성자 이름", example = "김사용자")
    private String writerName;

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품명", example = "나이키 에어조던 1")
    private String productName;

    @Schema(description = "리뷰 상태 (공개/비공개)", example = "ACTIVE")
    private ReviewStatus status;

    @Schema(description = "총 신고 수", example = "5")
    private Long totalReportCount;

    @Schema(description = "미처리 신고 수", example = "3")
    private Long unprocessedReportCount;

    @Schema(description = "신고 내역 목록")
    private List<ReportInfo> reports;

    @Schema(description = "리뷰 작성일시", example = "2025-08-28T10:30:00")
    private LocalDateTime reviewCreatedAt;

    @Getter
    @Builder
    @Schema(description = "신고 상세 정보")
    public static class ReportInfo {
        @Schema(description = "신고 ID", example = "1")
        private Long reportId;

        @Schema(description = "신고자 ID", example = "789")
        private Long reporterId;

        @Schema(description = "신고자 이름", example = "이신고자")
        private String reporterName;

        @Schema(description = "신고 사유", example = "INAPPROPRIATE_CONTENT")
        private ReportReason reason;

        @Schema(description = "신고 상세 설명", example = "부적절한 내용이 포함되어 신고합니다.")
        private String description;

        @Schema(description = "관리자 처리 여부", example = "false")
        private Boolean isProcessed;

        @Schema(description = "신고 일시", example = "2025-08-28T14:20:00")
        private LocalDateTime createdAt;
    }
}