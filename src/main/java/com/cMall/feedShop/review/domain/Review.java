package com.cMall.feedShop.review.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "size_fit", nullable = false)
    private SizeFit sizeFit;

    @Enumerated(EnumType.STRING)
    @Column(name = "cushion", nullable = false)
    private Cushion cushion;

    @Enumerated(EnumType.STRING)
    @Column(name = "stability", nullable = false)
    private Stability stability;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "points", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    @Column(name = "report_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer reportCount = 0;

    @Column(name = "is_blinded", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBlinded = false;

    @Column(name = "has_detailed_content", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean hasDetailedContent = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 🆕 이미지 연관관계 추가
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();


    @Builder
    public Review(String title, Integer rating, SizeFit sizeFit, Cushion cushion,
                  Stability stability, String content, User user, Product product) {
        validateTitle(title);
        validateRating(rating);
        validateContent(content);

        this.title = title;
        this.rating = rating;
        this.sizeFit = sizeFit;
        this.cushion = cushion;
        this.stability = stability;
        this.content = content;
        this.user = user;
        this.product = product;
        this.points = 0;
        this.reportCount = 0;
        this.isBlinded = false;
        this.hasDetailedContent = false;
        this.status = ReviewStatus.ACTIVE;
    }

    // 비즈니스 메서드 (SPRINT 1용 - 최소한만)
    public void addPoint() {
        this.points++;
    }

    public void removePoint() {
        if (this.points > 0) {
            this.points--;
        }
    }

    public boolean isActive() {
        return this.status == ReviewStatus.ACTIVE && !this.isBlinded;
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    // 🆕 이미지 관련 메서드들
    public void addImage(ReviewImage image) {
        this.images.add(image);
    }

    public void removeImage(ReviewImage image) {
        this.images.remove(image);
        image.delete();
    }

    public List<ReviewImage> getActiveImages() {
        return images.stream()
                .filter(ReviewImage::isActive)
                .sorted(Comparator.comparing(ReviewImage::getImageOrder))
                .toList();
    }

    public boolean hasImages() {
        return !getActiveImages().isEmpty();
    }

    public int getActiveImageCount() {
        return getActiveImages().size();
    }
    // 검증 메서드들
    public static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 제목은 필수입니다.");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("리뷰 제목은 100자를 초과할 수 없습니다.");
        }
    }

    public static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1점에서 5점 사이여야 합니다.");
        }
    }

    public static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("리뷰 내용은 1000자를 초과할 수 없습니다.");
        }
    }

    /**
     * 3요소 평가가 모두 완료되었는지 확인
     */
    public boolean hasCompleteReviewElements() {
        return this.sizeFit != null && this.cushion != null && this.stability != null;
    }


}