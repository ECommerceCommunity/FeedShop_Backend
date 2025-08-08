package com.cMall.feedShop.event.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventParticipant is a Querydsl query type for EventParticipant
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventParticipant extends EntityPathBase<EventParticipant> {

    private static final long serialVersionUID = -1231180231L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventParticipant eventParticipant = new QEventParticipant("eventParticipant");

    public final QEvent event;

    public final NumberPath<Long> feedId = createNumber("feedId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> participationDate = createDateTime("participationDate", java.time.LocalDateTime.class);

    public final EnumPath<com.cMall.feedShop.event.domain.enums.ParticipationStatus> participationStatus = createEnum("participationStatus", com.cMall.feedShop.event.domain.enums.ParticipationStatus.class);

    public QEventParticipant(String variable) {
        this(EventParticipant.class, forVariable(variable), INITS);
    }

    public QEventParticipant(Path<? extends EventParticipant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventParticipant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventParticipant(PathMetadata metadata, PathInits inits) {
        this(EventParticipant.class, metadata, inits);
    }

    public QEventParticipant(Class<? extends EventParticipant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.event = inits.isInitialized("event") ? new QEvent(forProperty("event"), inits.get("event")) : null;
    }

}

