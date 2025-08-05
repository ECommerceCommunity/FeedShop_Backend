package com.cMall.feedShop.feed.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_hashtags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedHashtag extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;
    
    @Column(name = "tag", nullable = false, length = 100)
    private String tag;
    
    @Builder
    public FeedHashtag(Feed feed, String tag) {
        this.feed = feed;
        this.tag = tag;
    }
} 