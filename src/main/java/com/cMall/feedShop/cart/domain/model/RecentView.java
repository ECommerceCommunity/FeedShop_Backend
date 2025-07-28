package com.cMall.feedShop.cart.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class RecentView {
    @Id
    private Long viewId;
    private Long userId;
    private Long productId;
    private LocalDateTime viewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
