package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 팔로우 관계를 나타내는 엔티티
 * - 팔로워(follower): 팔로우를 하는 사용자
 * - 팔로잉(following): 팔로우를 받는 사용자
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_follows",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_follow_follower_following", 
                           columnNames = {"follower_id", "following_id"})
       },
       indexes = {
           @Index(name = "idx_user_follow_follower", columnList = "follower_id"),
           @Index(name = "idx_user_follow_following", columnList = "following_id"),
           @Index(name = "idx_user_follow_created_at", columnList = "created_at")
       })
public class UserFollow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_follow_id")
    private Long id;

    /**
     * 팔로워 (팔로우를 하는 사용자)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /**
     * 팔로잉 (팔로우를 받는 사용자)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Builder
    public UserFollow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    /**
     * 팔로우 관계 검증
     * - 자기 자신을 팔로우할 수 없음
     */
    public void validateFollow() {
        if (follower.getId().equals(following.getId())) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }
    }
}
