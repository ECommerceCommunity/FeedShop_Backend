package com.cMall.feedShop.cart.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class Wishlist {
    @Id
    private Long wishlistId;
    private Long userId;
    private Long productId;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
