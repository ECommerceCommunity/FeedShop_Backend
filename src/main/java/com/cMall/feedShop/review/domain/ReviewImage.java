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
        return baseUrl + "/" + this.filePath;
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