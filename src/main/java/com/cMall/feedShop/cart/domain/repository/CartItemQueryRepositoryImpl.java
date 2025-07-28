package com.cMall.feedShop.cart.domain.repository;

import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.model.QCart;
import com.cMall.feedShop.cart.domain.model.QCartItem;
import com.cMall.feedShop.user.domain.model.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartItemQueryRepositoryImpl implements CartItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CartItem> findByUserIdWithCart(Long userId) {
        QCartItem cartItem = QCartItem.cartItem;
        QCart cart = QCart.cart;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(cartItem)
                .join(cartItem.cart, cart).fetchJoin()
                .join(cart.user, user).fetchJoin()
                .where(user.id.eq(userId))
                .orderBy(cartItem.createdAt.desc())
                .fetch();
    }

    @Override
    public Optional<CartItem> findByCartItemIdAndUserId(Long cartItemId, Long userId) {
        QCartItem cartItem = QCartItem.cartItem;
        QCart cart = QCart.cart;
        QUser user = QUser.user;

        CartItem result = queryFactory
                .selectFrom(cartItem)
                .join(cartItem.cart, cart).fetchJoin()
                .join(cart.user, user).fetchJoin()
                .where(
                        cartItem.cartItemId.eq(cartItemId)
                                .and(user.id.eq(userId))
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}