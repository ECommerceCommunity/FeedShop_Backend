package com.cMall.feedShop.feed.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeed is a Querydsl query type for Feed
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeed extends EntityPathBase<Feed> {

    private static final long serialVersionUID = 633142990L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeed feed = new QFeed("feed");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    public final NumberPath<Integer> commentCount = createNumber("commentCount", Integer.class);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final com.cMall.feedShop.event.domain.QEvent event;

    public final EnumPath<FeedType> feedType = createEnum("feedType", FeedType.class);

    public final ListPath<FeedHashtag, QFeedHashtag> hashtags = this.<FeedHashtag, QFeedHashtag>createList("hashtags", FeedHashtag.class, QFeedHashtag.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<FeedImage, QFeedImage> images = this.<FeedImage, QFeedImage>createList("images", FeedImage.class, QFeedImage.class, PathInits.DIRECT2);

    public final StringPath instagramId = createString("instagramId");

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final com.cMall.feedShop.order.domain.model.QOrderItem orderItem;

    public final NumberPath<Integer> participantVoteCount = createNumber("participantVoteCount", Integer.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.cMall.feedShop.user.domain.model.QUser user;

    public final ListPath<FeedVote, QFeedVote> votes = this.<FeedVote, QFeedVote>createList("votes", FeedVote.class, QFeedVote.class, PathInits.DIRECT2);

    public QFeed(String variable) {
        this(Feed.class, forVariable(variable), INITS);
    }

    public QFeed(Path<? extends Feed> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeed(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeed(PathMetadata metadata, PathInits inits) {
        this(Feed.class, metadata, inits);
    }

    public QFeed(Class<? extends Feed> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.event = inits.isInitialized("event") ? new com.cMall.feedShop.event.domain.QEvent(forProperty("event"), inits.get("event")) : null;
        this.orderItem = inits.isInitialized("orderItem") ? new com.cMall.feedShop.order.domain.model.QOrderItem(forProperty("orderItem"), inits.get("orderItem")) : null;
        this.user = inits.isInitialized("user") ? new com.cMall.feedShop.user.domain.model.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

