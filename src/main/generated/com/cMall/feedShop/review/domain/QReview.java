package com.cMall.feedShop.review.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReview is a Querydsl query type for Review
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReview extends EntityPathBase<Review> {

    private static final long serialVersionUID = 295084866L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReview review = new QReview("review");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.cMall.feedShop.review.domain.enums.Cushion> cushion = createEnum("cushion", com.cMall.feedShop.review.domain.enums.Cushion.class);

    public final BooleanPath hasDetailedContent = createBoolean("hasDetailedContent");

    public final ListPath<ReviewImage, QReviewImage> images = this.<ReviewImage, QReviewImage>createList("images", ReviewImage.class, QReviewImage.class, PathInits.DIRECT2);

    public final BooleanPath isBlinded = createBoolean("isBlinded");

    public final NumberPath<Integer> points = createNumber("points", Integer.class);

    public final com.cMall.feedShop.product.domain.model.QProduct product;

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final NumberPath<Integer> reportCount = createNumber("reportCount", Integer.class);

    public final NumberPath<Long> reviewId = createNumber("reviewId", Long.class);

    public final EnumPath<com.cMall.feedShop.review.domain.enums.SizeFit> sizeFit = createEnum("sizeFit", com.cMall.feedShop.review.domain.enums.SizeFit.class);

    public final EnumPath<com.cMall.feedShop.review.domain.enums.Stability> stability = createEnum("stability", com.cMall.feedShop.review.domain.enums.Stability.class);

    public final EnumPath<com.cMall.feedShop.review.domain.enums.ReviewStatus> status = createEnum("status", com.cMall.feedShop.review.domain.enums.ReviewStatus.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.cMall.feedShop.user.domain.model.QUser user;

    public QReview(String variable) {
        this(Review.class, forVariable(variable), INITS);
    }

    public QReview(Path<? extends Review> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReview(PathMetadata metadata, PathInits inits) {
        this(Review.class, metadata, inits);
    }

    public QReview(Class<? extends Review> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new com.cMall.feedShop.product.domain.model.QProduct(forProperty("product"), inits.get("product")) : null;
        this.user = inits.isInitialized("user") ? new com.cMall.feedShop.user.domain.model.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

