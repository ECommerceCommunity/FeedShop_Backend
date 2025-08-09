package com.cMall.feedShop.event.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBattleMatch is a Querydsl query type for BattleMatch
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBattleMatch extends EntityPathBase<BattleMatch> {

    private static final long serialVersionUID = -534954515L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBattleMatch battleMatch = new QBattleMatch("battleMatch");

    public final NumberPath<Long> battleMatchId = createNumber("battleMatchId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final EnumPath<com.cMall.feedShop.event.domain.enums.MatchStatus> matchStatus = createEnum("matchStatus", com.cMall.feedShop.event.domain.enums.MatchStatus.class);

    public final QEventParticipant participant1;

    public final QEventParticipant participant2;

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final QEventParticipant winner;

    public QBattleMatch(String variable) {
        this(BattleMatch.class, forVariable(variable), INITS);
    }

    public QBattleMatch(Path<? extends BattleMatch> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBattleMatch(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBattleMatch(PathMetadata metadata, PathInits inits) {
        this(BattleMatch.class, metadata, inits);
    }

    public QBattleMatch(Class<? extends BattleMatch> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.participant1 = inits.isInitialized("participant1") ? new QEventParticipant(forProperty("participant1"), inits.get("participant1")) : null;
        this.participant2 = inits.isInitialized("participant2") ? new QEventParticipant(forProperty("participant2"), inits.get("participant2")) : null;
        this.winner = inits.isInitialized("winner") ? new QEventParticipant(forProperty("winner"), inits.get("winner")) : null;
    }

}

