package com.cMall.feedShop.review.application.dto.request;

import com.cMall.feedShop.review.domain.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 신고 요청")
public class ReviewReportRequest {

    @NotNull(message = "신고 사유는 필수입니다.")
    @Schema(description = "신고 사유", example = "ABUSIVE_LANGUAGE")
    private ReportReason reason;

    @Size(max = 500, message = "신고 상세 설명은 500자 이하로 입력해주세요.")
    @Schema(description = "신고 상세 설명 (선택사항)", example = "욕설이 포함된 리뷰입니다.")
    private String description;

    @Builder
    public ReviewReportRequest(ReportReason reason, String description) {
        this.reason = reason;
        this.description = description;
    }
}