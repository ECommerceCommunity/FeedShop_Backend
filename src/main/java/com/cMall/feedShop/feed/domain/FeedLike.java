package com.cMall.feedShop.feed.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "feed_likes",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_feed_like_feed_user", columnNames = {"feed_id", "user_id"})
       },
       indexes = {
           @Index(name = "idx_feed_like_feed", columnList = "feed_id"),
           @Index(name = "idx_feed_like_user", columnList = "user_id")
       })
public class FeedLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 표시용 닉네임(비정규화 저장)
     * - 목록 조회시 조인 없이 표시 가능
     * - 변경 가능성 있으므로 최신값이 필요하면 UserProfile을 우선 조회
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    @Builder
    public FeedLike(Feed feed, User user, String nickname) {
        this.feed = feed;
        this.user = user;
        this.nickname = nickname;
    }
}
