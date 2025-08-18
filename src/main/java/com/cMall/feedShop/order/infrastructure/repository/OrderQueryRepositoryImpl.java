package com.cMall.feedShop.order.infrastructure.repository;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.QOrder;
import com.cMall.feedShop.order.domain.model.QOrderItem;
import com.cMall.feedShop.product.domain.model.QProduct;
import com.cMall.feedShop.product.domain.model.QProductImage;
import com.cMall.feedShop.product.domain.model.QProductOption;
import com.cMall.feedShop.store.domain.model.QStore;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findOrdersBySellerId(Long sellerId, Pageable pageable) {
        return findOrdersBySellerIdWithCondition(sellerId, null, pageable);
    }

    @Override
    public Page<Order> findOrdersBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable) {
        return findOrdersBySellerIdWithCondition(sellerId, status, pageable);
    }

    // 사용자별 주문 목록 조회 (EntityGraph 대체)
    public Page<Order> findByUserOrderByCreatedAtDesc(Long userId, Pageable pageable) {
        QOrder order = QOrder.order;

        List<Order> orders = createBaseQuery()
                .where(order.user.id.eq(userId))
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(order.user.id.eq(userId));

        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    // 사용자별 + 상태별 주문 목록 조회 (EntityGraph 대체)
    public Page<Order> findByUserAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, Pageable pageable) {
        QOrder order = QOrder.order;

        List<Order> orders = createBaseQuery()
                .where(order.user.id.eq(userId).and(order.status.eq(status)))
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(order.user.id.eq(userId).and(order.status.eq(status)));

        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    // 사용자별 특정 주문 조회 (EntityGraph 대체)
    public Optional<Order> findByOrderIdAndUserId(Long orderId, Long userId) {
        QOrder order = QOrder.order;

        Order result = createBaseQuery()
                .where(order.orderId.eq(orderId).and(order.user.id.eq(userId)))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    private Page<Order> findOrdersBySellerIdWithCondition(Long sellerId, OrderStatus status, Pageable pageable) {
        QOrder order = QOrder.order;

        List<Order> orders = createBaseQuery()
                .where(
                        sellerIdEq(sellerId),
                        statusEq(status)
                )
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = createCountQuery()
                .where(
                        sellerIdEq(sellerId),
                        statusEq(status)
                );

        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    private JPAQuery<Order> createBaseQuery() {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProductOption productOption = QProductOption.productOption;
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;
        QStore store = QStore.store;

        return queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderItems, orderItem).fetchJoin()
                .leftJoin(orderItem.productOption, productOption).fetchJoin()
                .leftJoin(productOption.product, product).fetchJoin()
                .leftJoin(product.store, store).fetchJoin()
                .leftJoin(orderItem.productImage, productImage).fetchJoin();
    }

    private JPAQuery<Long> createCountQuery() {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProductOption productOption = QProductOption.productOption;
        QProduct product = QProduct.product;
        QStore store = QStore.store;

        return queryFactory
                .select(order.countDistinct())
                .from(order)
                .leftJoin(order.orderItems, orderItem)
                .leftJoin(orderItem.productOption, productOption)
                .leftJoin(productOption.product, product)
                .leftJoin(product.store, store);
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        QStore store = QStore.store;
        return sellerId != null ? store.sellerId.eq(sellerId) : null;
    }

    private BooleanExpression statusEq(OrderStatus status) {
        QOrder order = QOrder.order;
        return status != null ? order.status.eq(status) : null;
    }

    /**
     * 주문 ID와 판매자 ID로 주문 조회 (판매자 권한 검증용)
     * @param orderId 주문 ID
     * @param sellerId 판매자 ID
     * @return 조회된 주문 Optional
     */
    public Optional<Order> findByOrderIdAndSellerId(Long orderId, Long sellerId) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProductOption productOption = QProductOption.productOption;
        QProduct product = QProduct.product;
        QStore store = QStore.store;
        QProductImage productImage = QProductImage.productImage;

        Order result = queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderItems, orderItem).fetchJoin()
                .leftJoin(orderItem.productOption, productOption).fetchJoin()
                .leftJoin(productOption.product, product).fetchJoin()
                .leftJoin(product.store, store).fetchJoin()
                .leftJoin(orderItem.productImage, productImage).fetchJoin()
                .where(
                        order.orderId.eq(orderId)
                                .and(store.sellerId.eq(sellerId))
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Long countByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus) {
        QOrder order = QOrder.order;
        
        Long count = queryFactory
                .select(order.count())
                .from(order)
                .where(
                        order.user.id.eq(userId),
                        order.status.eq(orderStatus)
                )
                .fetchOne();
        
        return count != null ? count : 0L;
    }

    @Override
    public Long findTotalOrderAmountByUserId(Long userId) {
        QOrder order = QOrder.order;
        
        BigDecimal totalAmount = queryFactory
                .select(order.finalPrice.sum())
                .from(order)
                .where(
                        order.user.id.eq(userId),
                        order.status.eq(OrderStatus.DELIVERED)
                )
                .fetchOne();
        
        return totalAmount != null ? totalAmount.longValue() : 0L;
    }
}