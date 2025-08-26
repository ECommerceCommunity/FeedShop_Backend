package com.cMall.feedShop.feed.domain.entity;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "feed_type", nullable = false)
    private FeedType feedType = FeedType.DAILY;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "instagram_id", length = 100)
    private String instagramId;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;
    
    @Column(name = "participant_vote_count", nullable = false)
    private Integer participantVoteCount = 0;
    
    // Soft Delete 필드
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // 연관관계 매핑
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedHashtag> hashtags = new ArrayList<>();
    
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<FeedImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedVote> votes = new ArrayList<>();
    
    @Builder
    public Feed(Event event, OrderItem orderItem, User user, 
                String title, String content, String instagramId) {
        this.event = event;
        this.orderItem = orderItem;
        this.user = user;
        
        // 이벤트 참여 여부에 따른 피드 타입 자동 결정
        this.feedType = (event != null) ? FeedType.EVENT : FeedType.DAILY;
        
        this.title = title;
        this.content = content;
        this.instagramId = instagramId;
    }
    
    // Soft Delete 메서드
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    public void addHashtag(String tag) {
        FeedHashtag hashtag = FeedHashtag.builder()
                .feed(this)
                .tag(tag)
                .build();
        this.hashtags.add(hashtag);
    }
    
    // 해시태그 일괄 추가 메서드
    public void addHashtags(List<String> tags) {
        if (tags != null) {
            tags.forEach(this::addHashtag);
        }
    }
    
    // 이미지 추가 메서드
    public void addImage(String imageUrl, Integer sortOrder) {
        FeedImage image = FeedImage.builder()
                .feed(this)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
        this.images.add(image);
    }
    
    // 이미지 일괄 추가 메서드
    public void addImages(List<String> imageUrls) {
        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                addImage(imageUrls.get(i), i);
            }
        }
    }
    
    // 좋아요 수 증가
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    // 좋아요 수 감소
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    // 댓글 수 증가
    public void incrementCommentCount() {
        this.commentCount++;
    }
    
    // 댓글 수 감소
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
    
    // 투표 수 증가
    public void incrementVoteCount() {
        this.participantVoteCount++;
    }
    
    // 투표 수 감소
    public void decrementVoteCount() {
        if (this.participantVoteCount > 0) {
            this.participantVoteCount--;
        }
    }
    
    // 피드 내용 업데이트
    public void updateContent(String title, String content, String instagramId) {
        this.title = title;
        this.content = content;
        this.instagramId = instagramId;
    }

    /**
     * 피드가 삭제되었는지 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 피드 상세 조회 가능 여부 확인
     */
    public boolean isViewable() {
        return !isDeleted();
    }

    /**
     * 이벤트 피드인지 확인
     */
    public boolean isEventFeed() {
        return this.feedType == FeedType.EVENT && this.event != null;
    }

    /**
     * 데일리 피드인지 확인
     */
    public boolean isDailyFeed() {
        return this.feedType == FeedType.DAILY;
    }
} 