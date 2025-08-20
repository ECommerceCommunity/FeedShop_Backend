package com.cMall.feedShop.cart.infrastructure.repository;

import com.cMall.feedShop.cart.application.dto.response.info.WishlistInfo;
import com.cMall.feedShop.cart.domain.model.QWishList;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.product.domain.model.QProduct;
import com.cMall.feedShop.product.domain.model.QProductImage;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WishlistQueryRepositoryImpl implements WishlistQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<WishlistInfo> findWishlistByUserId(Long userId, Pageable pageable) {
        QWishList wishlist = QWishList.wishList;
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        // QueryDSL Projections.constructor로 직접 DTO 매핑
        List<WishlistInfo> content = queryFactory
                .select(Projections.constructor(WishlistInfo.class,
                        wishlist.wishlistId,
                        product.productId,
                        product.name,
                        // 서브쿼리로 메인 이미지 URL 조회
                        JPAExpressions
                                .select(productImage.url.min())
                                .from(productImage)
                                .where(productImage.product.eq(product)
                                        .and(productImage.type.eq(ImageType.MAIN))),
                        product.price,
                        product.discountType,
                        product.discountValue,
                        wishlist.createdAt
                ))
                .from(wishlist)
                .join(wishlist.product, product)
                .where(wishlist.user.id.eq(userId)
                        .and(wishlist.deletedAt.isNull()))
                .orderBy(wishlist.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(wishlist.count())
                .from(wishlist)
                .where(wishlist.user.id.eq(userId)
                        .and(wishlist.deletedAt.isNull()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public long countWishlistByUserId(Long userId) {
        QWishList wishlist = QWishList.wishList;

        Long count = queryFactory
                .select(wishlist.count())
                .from(wishlist)
                .where(
                        wishlist.user.id.eq(userId)
                                .and(wishlist.deletedAt.isNull())
                )
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    public void increaseWishCount(Long productId) {
        QProduct product = QProduct.product;

        queryFactory
                .update(product)
                .set(product.wishNumber, product.wishNumber.add(1))
                .where(product.productId.eq(productId))
                .execute();
    }

    @Override
    public void decreaseWishCount(Long productId) {
        QProduct product = QProduct.product;

        queryFactory
                .update(product)
                .set(product.wishNumber,
                        new CaseBuilder()
                                .when(product.wishNumber.gt(0))
                                .then(product.wishNumber.subtract(1))
                                .otherwise(0))
                .where(product.productId.eq(productId))
                .execute();
    }

    @Override
    public Optional<WishList> findByUserIdAndProductIdAndDeletedAtIsNull(Long userId, Long productId) {
        QWishList wishList = QWishList.wishList;

        WishList result = queryFactory
                .selectFrom(wishList)
                .where(
                        wishList.user.id.eq(userId)
                                .and(wishList.product.productId.eq(productId))  // productId 명시적 사용
                                .and(wishList.deletedAt.isNull())
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

}