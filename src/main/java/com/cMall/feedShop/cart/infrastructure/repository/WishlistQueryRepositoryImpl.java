package com.cMall.feedShop.cart.infrastructure.repository;

import com.cMall.feedShop.product.domain.model.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WishlistQueryRepositoryImpl implements WishlistQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public void increaseWishCount(Long productId) {
        QProduct product = QProduct.product;

        queryFactory
                .update(product)
                .set(product.wishNumber, product.wishNumber.add(1))
                .where(product.productId.eq(productId))
                .execute();
    }

}