package com.cMall.feedShop.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="user_badges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name="badge_type", nullable=false)
    private BadgeType badgeType;

    @Column(name="awarded_at", nullable=false)
    private LocalDateTime awardedAt;

    @Column(name="is_displayed", nullable=false)
    private Boolean isDisplayed = true;

    @Builder
    public UserBadge(User user, BadgeType badgeType, LocalDateTime awardedAt, Boolean isDisplayed) {
        this.user = user;
        this.badgeType = badgeType;
        this.awardedAt = awardedAt != null ? awardedAt : LocalDateTime.now();
        this.isDisplayed = isDisplayed != null ? isDisplayed : true;
    }

    public void toggleDisplay() {
        this.isDisplayed = !this.isDisplayed;
    }

    public String getBadgeName() {
        return badgeType.getName();
    }

    public String getBadgeDescription() {
        return badgeType.getDescription();
    }

    public String getBadgeImageUrl() {
        return badgeType.getImageUrl();
    }
}
