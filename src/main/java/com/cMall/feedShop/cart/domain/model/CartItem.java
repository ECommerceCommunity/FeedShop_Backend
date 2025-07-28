package com.cMall.feedShop.cart.domain.model;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "selected", nullable = false, columnDefinition = "boolean default true")
    private Boolean selected;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Builder
    public CartItem(Cart cart, Long optionId, Long imageId, Integer quantity) {
        this.cart = cart;
        this.optionId = optionId;
        this.imageId = imageId;
        this.quantity = quantity;
        this.selected = true;
    }

    public void updateQuantity(Integer newQuantity) {
        if (newQuantity < 1) {
            throw new CartException.CartZeroQuantityException();
        }
        this.quantity = newQuantity;
    }

    public void updateSelected(Boolean selected) {
        if (selected != null) {
            this.selected = selected;
        }
    }
}
