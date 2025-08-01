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
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
    
    @Builder
    public FeedVote(Feed feed, User voter, Event event) {
        this.feed = feed;
        this.voter = voter;
        this.event = event;
    }
} 