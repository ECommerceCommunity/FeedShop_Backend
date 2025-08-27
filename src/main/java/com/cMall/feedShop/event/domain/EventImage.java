package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", length = 255)
    private String storedFilename;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "image_order", nullable = false)
    @Builder.Default
    private Integer imageOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Builder
    public EventImage(Event event, String imageUrl, Integer imageOrder) {
        this.event = event;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder != null ? imageOrder : 0;
    }

    @Builder
    public EventImage(Event event, String originalFilename, String storedFilename, 
                     String filePath, Long fileSize, String contentType, Integer imageOrder) {
        this.event = event;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.imageOrder = imageOrder != null ? imageOrder : 0;
        this.imageUrl = filePath; // filePath를 imageUrl로 사용
    }

    /**
     * 이미지 순서 업데이트
     */
    public void updateImageOrder(Integer imageOrder) {
        this.imageOrder = imageOrder != null ? imageOrder : this.imageOrder;
    }

    /**
     * 이미지 URL 업데이트
     */
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * 이벤트 설정 (양방향 연관관계)
     */
    public void setEvent(Event event) {
        this.event = event;
    }
}
