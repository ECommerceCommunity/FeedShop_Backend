package com.cMall.feedShop.feed.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedImage extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;
    
    @Column(name = "image_url", nullable = false, length = 100)
    private String imageUrl;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @Builder
    public FeedImage(Feed feed, String imageUrl, Integer sortOrder) {
        this.feed = feed;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
} 