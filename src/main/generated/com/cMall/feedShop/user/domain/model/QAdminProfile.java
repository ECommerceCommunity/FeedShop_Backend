package com.cMall.feedShop.user.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminProfile is a Querydsl query type for AdminProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminProfile extends EntityPathBase<AdminProfile> {

    private static final long serialVersionUID = 2016757532L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminProfile adminProfile = new QAdminProfile("adminProfile");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QAdminProfile(String variable) {
        this(AdminProfile.class, forVariable(variable), INITS);
    }

    public QAdminProfile(Path<? extends AdminProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminProfile(PathMetadata metadata, PathInits inits) {
        this(AdminProfile.class, metadata, inits);
    }

    public QAdminProfile(Class<? extends AdminProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

