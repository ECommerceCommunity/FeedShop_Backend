package com.cMall.feedShop.event.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventRanking is a Querydsl query type for EventRanking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventRanking extends EntityPathBase<EventRanking> {

    private static final long serialVersionUID = 474021436L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventRanking eventRanking = new QEventRanking("eventRanking");

    public final DateTimePath<java.time.LocalDateTime> calculatedAt = createDateTime("calculatedAt", java.time.LocalDateTime.class);

    public final QEventParticipant participant;

    public final NumberPath<Long> rankingId = createNumber("rankingId", Long.class);

    public final NumberPath<Integer> rankPosition = createNumber("rankPosition", Integer.class);

    public final NumberPath<Integer> voteCount = createNumber("voteCount", Integer.class);

    public QEventRanking(String variable) {
        this(EventRanking.class, forVariable(variable), INITS);
    }

    public QEventRanking(Path<? extends EventRanking> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventRanking(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventRanking(PathMetadata metadata, PathInits inits) {
        this(EventRanking.class, metadata, inits);
    }

    public QEventRanking(Class<? extends EventRanking> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.participant = inits.isInitialized("participant") ? new QEventParticipant(forProperty("participant"), inits.get("participant")) : null;
    }

}

