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
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
@Slf4j
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

    // 이미지 연관관계
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

    // 기존 비즈니스 메서드들
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

    // 이미지 관련 메서드들
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

    // =================== 리뷰 수정 관련 메서드들 ===================

    /**
     * 리뷰 정보 업데이트 (기본 정보만)
     */
    public void updateReviewInfo(String title, Integer rating, String content,
                                 SizeFit sizeFit, Cushion cushion, Stability stability) {
        // 입력값 검증 (기존 검증 메서드 재사용)
        validateTitle(title);
        validateRating(rating);
        validateContent(content);

        // 3요소 검증
        if (sizeFit == null || cushion == null || stability == null) {
            throw new IllegalArgumentException("3요소 평가(사이즈, 쿠션, 안정성)는 모두 필수입니다.");
        }

        // 실제 업데이트
        this.title = title;
        this.rating = rating;
        this.content = content;
        this.sizeFit = sizeFit;
        this.cushion = cushion;
        this.stability = stability;
    }

    /**
     * 리뷰 제목만 수정
     */
    public void updateTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    /**
     * 리뷰 평점만 수정
     */
    public void updateRating(Integer rating) {
        validateRating(rating);
        this.rating = rating;
    }

    /**
     * 리뷰 내용만 수정
     */
    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
    }

    /**
     * 3요소 평가 수정
     */
    public void update3Elements(SizeFit sizeFit, Cushion cushion, Stability stability) {
        if (sizeFit == null || cushion == null || stability == null) {
            throw new IllegalArgumentException("3요소 평가는 모두 필수입니다.");
        }

        this.sizeFit = sizeFit;
        this.cushion = cushion;
        this.stability = stability;
    }

    /**
     * 리뷰 수정 권한 확인
     */
    public boolean canBeUpdatedBy(Long userId) {
        return isOwnedBy(userId) && isActive();
    }

    /**
     * 리뷰가 수정 가능한 상태인지 확인
     */
    public boolean isUpdatable() {
        return isActive(); // 활성 상태인 리뷰만 수정 가능
    }

    /**
     * 리뷰 수정 권한 검증 (예외 발생)
     */
    public void validateUpdatePermission(Long userId) {
        if (!canBeUpdatedBy(userId)) {
            if (!isOwnedBy(userId)) {
                throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
            }
            if (!isActive()) {
                throw new IllegalArgumentException("삭제되거나 숨김 처리된 리뷰는 수정할 수 없습니다.");
            }
        }
    }

    /**
     * 특정 이미지 삭제 (이미지 ID로)
     */
    public void removeImageById(Long imageId) {
        ReviewImage imageToRemove = this.images.stream()
                .filter(image -> image.getReviewImageId().equals(imageId))
                .findFirst()
                .orElse(null);

        if (imageToRemove != null) {
            removeImage(imageToRemove);
        }
    }

    /**
     * 여러 이미지 한 번에 삭제
     */
    public void removeImagesByIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        List<ReviewImage> imagesToRemove = this.images.stream()
                .filter(image -> imageIds.contains(image.getReviewImageId()))
                .toList();

        imagesToRemove.forEach(this::removeImage);
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

    /**
     * 리뷰를 삭제 상태로 변경 (소프트 삭제)
     */
    public void markAsDeleted() {
        this.status = ReviewStatus.DELETED;
        log.info("리뷰가 삭제 상태로 변경됨: reviewId={}", this.reviewId);
    }
}