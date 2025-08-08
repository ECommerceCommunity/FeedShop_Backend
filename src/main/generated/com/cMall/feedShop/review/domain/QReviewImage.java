package com.cMall.feedShop.review.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewImage is a Querydsl query type for ReviewImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewImage extends EntityPathBase<ReviewImage> {

    private static final long serialVersionUID = 384749369L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewImage reviewImage = new QReviewImage("reviewImage");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    public final StringPath contentType = createString("contentType");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath filePath = createString("filePath");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final NumberPath<Integer> imageOrder = createNumber("imageOrder", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath originalFilename = createString("originalFilename");

    public final QReview review;

    public final NumberPath<Long> reviewImageId = createNumber("reviewImageId", Long.class);

    public final StringPath storedFilename = createString("storedFilename");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReviewImage(String variable) {
        this(ReviewImage.class, forVariable(variable), INITS);
    }

    public QReviewImage(Path<? extends ReviewImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewImage(PathMetadata metadata, PathInits inits) {
        this(ReviewImage.class, metadata, inits);
    }

    public QReviewImage(Class<? extends ReviewImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QReview(forProperty("review"), inits.get("review")) : null;
    }

}

