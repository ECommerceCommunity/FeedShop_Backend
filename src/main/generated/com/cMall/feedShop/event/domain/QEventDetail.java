package com.cMall.feedShop.event.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventDetail is a Querydsl query type for EventDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventDetail extends EntityPathBase<EventDetail> {

    private static final long serialVersionUID = -104559221L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventDetail eventDetail = new QEventDetail("eventDetail");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    public final DatePath<java.time.LocalDate> announcement = createDate("announcement", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final QEvent event;

    public final DatePath<java.time.LocalDate> eventEndDate = createDate("eventEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> eventStartDate = createDate("eventStartDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath participationMethod = createString("participationMethod");

    public final StringPath precautions = createString("precautions");

    public final DatePath<java.time.LocalDate> purchaseEndDate = createDate("purchaseEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> purchaseStartDate = createDate("purchaseStartDate", java.time.LocalDate.class);

    public final StringPath selectionCriteria = createString("selectionCriteria");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEventDetail(String variable) {
        this(EventDetail.class, forVariable(variable), INITS);
    }

    public QEventDetail(Path<? extends EventDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventDetail(PathMetadata metadata, PathInits inits) {
        this(EventDetail.class, metadata, inits);
    }

    public QEventDetail(Class<? extends EventDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.event = inits.isInitialized("event") ? new QEvent(forProperty("event"), inits.get("event")) : null;
    }

}

