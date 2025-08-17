package com.cMall.feedShop.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;
    
    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "reference_id")
    private Long referenceId; // 관련 엔티티 ID (리뷰 ID, 주문 ID 등)
    
    @Column(name = "reference_type")
    private String referenceType; // 관련 엔티티 타입 (REVIEW, ORDER 등)
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public UserActivity(User user, ActivityType activityType, String description, 
                       Long referenceId, String referenceType) {
        this.user = user;
        this.activityType = activityType;
        this.pointsEarned = activityType.getPoints();
        this.description = description != null ? description : activityType.getDescription();
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.createdAt = LocalDateTime.now();
    }
}
