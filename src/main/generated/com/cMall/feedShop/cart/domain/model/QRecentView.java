package com.cMall.feedShop.cart.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRecentView is a Querydsl query type for RecentView
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecentView extends EntityPathBase<RecentView> {

    private static final long serialVersionUID = 1965041719L;

    public static final QRecentView recentView = new QRecentView("recentView");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> viewedAt = createDateTime("viewedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> viewId = createNumber("viewId", Long.class);

    public QRecentView(String variable) {
        super(RecentView.class, forVariable(variable));
    }

    public QRecentView(Path<? extends RecentView> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRecentView(PathMetadata metadata) {
        super(RecentView.class, metadata);
    }

}

