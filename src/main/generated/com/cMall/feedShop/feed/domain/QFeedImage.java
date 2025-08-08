package com.cMall.feedShop.feed.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedImage is a Querydsl query type for FeedImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedImage extends EntityPathBase<FeedImage> {

    private static final long serialVersionUID = -495989971L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedImage feedImage = new QFeedImage("feedImage");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QFeed feed;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFeedImage(String variable) {
        this(FeedImage.class, forVariable(variable), INITS);
    }

    public QFeedImage(Path<? extends FeedImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedImage(PathMetadata metadata, PathInits inits) {
        this(FeedImage.class, metadata, inits);
    }

    public QFeedImage(Class<? extends FeedImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feed = inits.isInitialized("feed") ? new QFeed(forProperty("feed"), inits.get("feed")) : null;
    }

}

