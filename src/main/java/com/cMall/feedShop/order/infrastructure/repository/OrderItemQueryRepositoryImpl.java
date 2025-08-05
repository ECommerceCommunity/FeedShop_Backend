package com.cMall.feedShop.order.infrastructure.repository;

import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.QOrder;
import com.cMall.feedShop.order.domain.model.QOrderItem;
import com.cMall.feedShop.product.domain.model.QProduct;
import com.cMall.feedShop.product.domain.model.QProductImage;
import com.cMall.feedShop.product.domain.model.QProductOption;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderItemQueryRepositoryImpl implements OrderItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PurchasedItemInfo> findPurchasedItemsByUserId(Long userId) {
        QOrderItem orderItem = QOrderItem.orderItem;
        QOrder order = QOrder.order;
        QProductOption productOption = QProductOption.productOption;
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        return queryFactory
                .select(Projections.constructor(
                        PurchasedItemInfo.class,
                        orderItem.orderItemId,
                        product.productId,
                        product.name,
                        productImage.url,
                        orderItem.createdAt
                ))
                .from(orderItem)
                .join(orderItem.order, order)
                .join(orderItem.productOption, productOption)
                .join(productOption.product, product)
                .join(orderItem.productImage, productImage)
                .where(
                        order.user.id.eq(userId)
                                .and(order.status.eq(OrderStatus.ORDERED))
                )
                .orderBy(orderItem.createdAt.desc())
                .fetch();
    }
}
