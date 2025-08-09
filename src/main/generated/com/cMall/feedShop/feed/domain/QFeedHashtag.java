package com.cMall.feedShop.feed.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedHashtag is a Querydsl query type for FeedHashtag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedHashtag extends EntityPathBase<FeedHashtag> {

    private static final long serialVersionUID = -1119375074L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedHashtag feedHashtag = new QFeedHashtag("feedHashtag");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QFeed feed;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath tag = createString("tag");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFeedHashtag(String variable) {
        this(FeedHashtag.class, forVariable(variable), INITS);
    }

    public QFeedHashtag(Path<? extends FeedHashtag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedHashtag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedHashtag(PathMetadata metadata, PathInits inits) {
        this(FeedHashtag.class, metadata, inits);
    }

    public QFeedHashtag(Class<? extends FeedHashtag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feed = inits.isInitialized("feed") ? new QFeed(forProperty("feed"), inits.get("feed")) : null;
    }

}

