package com.cMall.feedShop.event.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventReward is a Querydsl query type for EventReward
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventReward extends EntityPathBase<EventReward> {

    private static final long serialVersionUID = 296338537L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventReward eventReward = new QEventReward("eventReward");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    public final StringPath conditionValue = createString("conditionValue");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QEvent event;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxRecipients = createNumber("maxRecipients", Integer.class);

    public final StringPath rewardValue = createString("rewardValue");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEventReward(String variable) {
        this(EventReward.class, forVariable(variable), INITS);
    }

    public QEventReward(Path<? extends EventReward> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventReward(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventReward(PathMetadata metadata, PathInits inits) {
        this(EventReward.class, metadata, inits);
    }

    public QEventReward(Class<? extends EventReward> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.event = inits.isInitialized("event") ? new QEvent(forProperty("event"), inits.get("event")) : null;
    }

}

