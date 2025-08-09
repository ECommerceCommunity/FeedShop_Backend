package com.cMall.feedShop.cart.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWishlist is a Querydsl query type for Wishlist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWishlist extends EntityPathBase<Wishlist> {

    private static final long serialVersionUID = 25217052L;

    public static final QWishlist wishlist = new QWishlist("wishlist");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final NumberPath<Long> wishlistId = createNumber("wishlistId", Long.class);

    public QWishlist(String variable) {
        super(Wishlist.class, forVariable(variable));
    }

    public QWishlist(Path<? extends Wishlist> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWishlist(PathMetadata metadata) {
        super(Wishlist.class, metadata);
    }

}

