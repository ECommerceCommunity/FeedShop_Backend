package com.cMall.feedShop.user.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="user_badges")
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="badge_name", nullable=false)
    private String badgeName;

    @Column(name="badge_description")
    private String badgeDescription;

    @Column(name = "badge_image_url")
    private String badgeImageUrl;

    @Column(name="awarded_at", nullable=false)
    private LocalDateTime awardedAt;
}
