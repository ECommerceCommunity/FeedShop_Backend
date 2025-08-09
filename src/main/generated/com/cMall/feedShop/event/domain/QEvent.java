package com.cMall.feedShop.event.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEvent is a Querydsl query type for Event
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEvent extends EntityPathBase<Event> {

    private static final long serialVersionUID = -458169190L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEvent event = new QEvent("event");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> createdBy = createDateTime("createdBy", java.time.LocalDateTime.class);

    public final com.cMall.feedShop.user.domain.model.QUser createdUser;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final QEventDetail eventDetail;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxParticipants = createNumber("maxParticipants", Integer.class);

    public final ListPath<EventReward, QEventReward> rewards = this.<EventReward, QEventReward>createList("rewards", EventReward.class, QEventReward.class, PathInits.DIRECT2);

    public final EnumPath<com.cMall.feedShop.event.domain.enums.EventStatus> status = createEnum("status", com.cMall.feedShop.event.domain.enums.EventStatus.class);

    public final EnumPath<com.cMall.feedShop.event.domain.enums.EventType> type = createEnum("type", com.cMall.feedShop.event.domain.enums.EventType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> updatedBy = createDateTime("updatedBy", java.time.LocalDateTime.class);

    public QEvent(String variable) {
        this(Event.class, forVariable(variable), INITS);
    }

    public QEvent(Path<? extends Event> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEvent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEvent(PathMetadata metadata, PathInits inits) {
        this(Event.class, metadata, inits);
    }

    public QEvent(Class<? extends Event> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.createdUser = inits.isInitialized("createdUser") ? new com.cMall.feedShop.user.domain.model.QUser(forProperty("createdUser"), inits.get("createdUser")) : null;
        this.eventDetail = inits.isInitialized("eventDetail") ? new QEventDetail(forProperty("eventDetail"), inits.get("eventDetail")) : null;
    }

}

