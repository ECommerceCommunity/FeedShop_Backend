package com.cMall.feedShop.event.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.cMall.feedShop.event.domain.enums.RewardKind;

@Entity
@Table(name = "reward_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private RewardKind type;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
