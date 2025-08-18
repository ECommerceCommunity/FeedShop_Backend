package com.cMall.feedShop.feed.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedVote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public FeedVote(Feed feed, User user) {
        this.feed = feed;
        this.user = user;
    }

    /**
     * 투표 작성자 확인
     */
    public boolean isVotedBy(Long userId) {
        return this.user.getId().equals(userId);
    }
} 