package com.cMall.feedShop.review.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.review.domain.enums.ReportReason;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_reports",
       uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "reporter_id"}))
@Getter
@NoArgsConstructor
public class ReviewReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_processed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isProcessed = false;

    @Builder
    public ReviewReport(Review review, User reporter, ReportReason reason, String description) {
        validateInputs(review, reporter, reason);
        
        this.review = review;
        this.reporter = reporter;
        this.reason = reason;
        this.description = description;
        this.isProcessed = false;
    }

    private void validateInputs(Review review, User reporter, ReportReason reason) {
        if (review == null) {
            throw new IllegalArgumentException("신고할 리뷰는 필수입니다.");
        }
        if (reporter == null) {
            throw new IllegalArgumentException("신고자 정보는 필수입니다.");
        }
        if (reason == null) {
            throw new IllegalArgumentException("신고 사유는 필수입니다.");
        }
        if (review.isOwnedBy(reporter.getId())) {
            throw new IllegalArgumentException("자신이 작성한 리뷰는 신고할 수 없습니다.");
        }
    }

    public void markAsProcessed() {
        this.isProcessed = true;
    }

    public boolean isProcessed() {
        return this.isProcessed;
    }
}