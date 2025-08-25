package com.cMall.feedShop.review.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_images")
@Getter
@NoArgsConstructor
public class ReviewImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private Long reviewImageId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, unique = true)
    private String storedFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Builder
    public ReviewImage(String originalFilename, String storedFilename, String filePath,
                       Long fileSize, String contentType, Integer imageOrder, Review review) {
        validateImageData(originalFilename, storedFilename, filePath, fileSize, contentType);

        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.imageOrder = imageOrder;
        this.review = review;
        this.isDeleted = false;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public boolean isActive() {
        return !this.isDeleted;
    }

    public String getFullImageUrl(String baseUrl) {
        // 이미 완전한 URL인 경우 그대로 반환
        if (this.filePath != null && (this.filePath.startsWith("http://") || this.filePath.startsWith("https://"))) {
            return this.filePath;
        }
        // 상대 경로인 경우 baseUrl과 조합
        return baseUrl + "/" + this.filePath;
    }

    // =================== 이미지 순서 관리 메서드들 ===================

    /**
     * 이미지 순서 변경
     */
    public void updateOrder(Integer newOrder) {
        if (newOrder == null || newOrder < 1) {
            throw new IllegalArgumentException("이미지 순서는 1 이상이어야 합니다.");
        }
        this.imageOrder = newOrder;
    }

    /**
     * 이미지 순서를 한 칸씩 앞으로 이동
     */
    public void moveUp() {
        if (this.imageOrder > 1) {
            this.imageOrder--;
        }
    }

    /**
     * 이미지 순서를 한 칸씩 뒤로 이동
     */
    public void moveDown() {
        this.imageOrder++;
    }

    /**
     * 현재 이미지가 첫 번째 이미지인지 확인
     */
    public boolean isFirst() {
        return this.imageOrder != null && this.imageOrder == 1;
    }

    /**
     * 두 이미지의 순서를 바꾸기
     */
    public void swapOrderWith(ReviewImage otherImage) {
        if (otherImage == null) {
            throw new IllegalArgumentException("바꿀 이미지가 null입니다.");
        }

        Integer tempOrder = this.imageOrder;
        this.imageOrder = otherImage.imageOrder;
        otherImage.imageOrder = tempOrder;
    }

    // =================== 이미지 정보 조회 메서드들 ===================

    /**
     * 이미지 파일 확장자 추출
     */
    public String getFileExtension() {
        if (originalFilename == null) {
            return "";
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
            return "";
        }

        return originalFilename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 이미지 파일 크기를 MB 단위로 반환
     */
    public double getFileSizeInMB() {
        if (fileSize == null) {
            return 0.0;
        }
        return fileSize / (1024.0 * 1024.0);
    }

    /**
     * 이미지 파일 크기를 읽기 쉬운 형태로 반환 (예: "2.5 MB", "156 KB")
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * 이미지가 특정 형식인지 확인
     */
    public boolean isImageType(String... types) {
        if (contentType == null) {
            return false;
        }

        for (String type : types) {
            if (contentType.toLowerCase().contains(type.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * JPEG 이미지인지 확인
     */
    public boolean isJpeg() {
        return isImageType("jpeg", "jpg");
    }

    /**
     * PNG 이미지인지 확인
     */
    public boolean isPng() {
        return isImageType("png");
    }

    /**
     * WebP 이미지인지 확인
     */
    public boolean isWebP() {
        return isImageType("webp");
    }

    // =================== 복사 및 비교 메서드들 ===================

    /**
     * 이미지 기본 정보만 복사 (새로운 ReviewImage 생성)
     * 리뷰 수정 시 이미지 복제가 필요한 경우 사용
     */
    public ReviewImage copyForNewReview(Review newReview, Integer newOrder) {
        return ReviewImage.builder()
                .originalFilename(this.originalFilename)
                .storedFilename(this.storedFilename)
                .filePath(this.filePath)
                .fileSize(this.fileSize)
                .contentType(this.contentType)
                .imageOrder(newOrder)
                .review(newReview)
                .build();
    }

    /**
     * 같은 파일인지 확인 (파일 경로로 비교)
     */
    public boolean isSameFile(ReviewImage other) {
        if (other == null) {
            return false;
        }
        return this.filePath != null && this.filePath.equals(other.filePath);
    }

    /**
     * 이미지 순서가 같은지 확인
     */
    public boolean hasSameOrder(ReviewImage other) {
        if (other == null) {
            return false;
        }
        return this.imageOrder != null && this.imageOrder.equals(other.imageOrder);
    }

    private void validateImageData(String originalFilename, String storedFilename,
                                   String filePath, Long fileSize, String contentType) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("원본 파일명은 필수입니다.");
        }
        if (storedFilename == null || storedFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("저장된 파일명은 필수입니다.");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("파일 경로는 필수입니다.");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다.");
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }
}