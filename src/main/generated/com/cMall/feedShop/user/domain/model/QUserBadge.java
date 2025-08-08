package com.cMall.feedShop.user.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserBadge is a Querydsl query type for UserBadge
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserBadge extends EntityPathBase<UserBadge> {

    private static final long serialVersionUID = -1182491114L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserBadge userBadge = new QUserBadge("userBadge");

    public final DateTimePath<java.time.LocalDateTime> awardedAt = createDateTime("awardedAt", java.time.LocalDateTime.class);

    public final StringPath badgeDescription = createString("badgeDescription");

    public final StringPath badgeImageUrl = createString("badgeImageUrl");

    public final StringPath badgeName = createString("badgeName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser user;

    public QUserBadge(String variable) {
        this(UserBadge.class, forVariable(variable), INITS);
    }

    public QUserBadge(Path<? extends UserBadge> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserBadge(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserBadge(PathMetadata metadata, PathInits inits) {
        this(UserBadge.class, metadata, inits);
    }

    public QUserBadge(Class<? extends UserBadge> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

