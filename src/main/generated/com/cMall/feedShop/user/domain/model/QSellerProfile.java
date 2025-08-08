package com.cMall.feedShop.user.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerProfile is a Querydsl query type for SellerProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerProfile extends EntityPathBase<SellerProfile> {

    private static final long serialVersionUID = 937131432L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerProfile sellerProfile = new QSellerProfile("sellerProfile");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    public final StringPath businessNumber = createString("businessNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QSellerProfile(String variable) {
        this(SellerProfile.class, forVariable(variable), INITS);
    }

    public QSellerProfile(Path<? extends SellerProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerProfile(PathMetadata metadata, PathInits inits) {
        this(SellerProfile.class, metadata, inits);
    }

    public QSellerProfile(Class<? extends SellerProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

