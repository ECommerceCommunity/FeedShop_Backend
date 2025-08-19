package com.cMall.feedShop.feed.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.event.domain.Event;
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
    @Column(name = "feed_vote_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @Builder
    public FeedVote(Event event, Feed feed, User voter) {
        this.event = event;
        this.feed = feed;
        this.voter = voter;
    }

    /**
     * 투표 작성자 확인
     */
    public boolean isVotedBy(Long userId) {
        return this.voter.getId().equals(userId);
    }

    /**
     * 피드 생성자 확인
     */
    public boolean isFeedCreatedBy(Long userId) {
        return this.feed.getUser().getId().equals(userId);
    }
} 