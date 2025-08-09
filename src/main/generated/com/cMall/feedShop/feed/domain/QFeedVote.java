package com.cMall.feedShop.feed.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedVote is a Querydsl query type for FeedVote
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedVote extends EntityPathBase<FeedVote> {

    private static final long serialVersionUID = -292704552L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedVote feedVote = new QFeedVote("feedVote");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.cMall.feedShop.event.domain.QEvent event;

    public final QFeed feed;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.cMall.feedShop.user.domain.model.QUser voter;

    public QFeedVote(String variable) {
        this(FeedVote.class, forVariable(variable), INITS);
    }

    public QFeedVote(Path<? extends FeedVote> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedVote(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedVote(PathMetadata metadata, PathInits inits) {
        this(FeedVote.class, metadata, inits);
    }

    public QFeedVote(Class<? extends FeedVote> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.event = inits.isInitialized("event") ? new com.cMall.feedShop.event.domain.QEvent(forProperty("event"), inits.get("event")) : null;
        this.feed = inits.isInitialized("feed") ? new QFeed(forProperty("feed"), inits.get("feed")) : null;
        this.voter = inits.isInitialized("voter") ? new com.cMall.feedShop.user.domain.model.QUser(forProperty("voter"), inits.get("voter")) : null;
    }

}

