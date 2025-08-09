package com.cMall.feedShop.user.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1143766413L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.cMall.feedShop.common.QBaseTimeEntity _super = new com.cMall.feedShop.common.QBaseTimeEntity(this);

    public final com.cMall.feedShop.cart.domain.model.QCart cart;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath loginId = createString("loginId");

    public final ListPath<com.cMall.feedShop.order.domain.model.Order, com.cMall.feedShop.order.domain.model.QOrder> orders = this.<com.cMall.feedShop.order.domain.model.Order, com.cMall.feedShop.order.domain.model.QOrder>createList("orders", com.cMall.feedShop.order.domain.model.Order.class, com.cMall.feedShop.order.domain.model.QOrder.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final DateTimePath<java.time.LocalDateTime> passwordChangedAt = createDateTime("passwordChangedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.cMall.feedShop.user.domain.enums.UserRole> role = createEnum("role", com.cMall.feedShop.user.domain.enums.UserRole.class);

    public final EnumPath<com.cMall.feedShop.user.domain.enums.UserStatus> status = createEnum("status", com.cMall.feedShop.user.domain.enums.UserStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUserPoint userPoint;

    public final QUserProfile userProfile;

    public final StringPath verificationToken = createString("verificationToken");

    public final DateTimePath<java.time.LocalDateTime> verificationTokenExpiry = createDateTime("verificationTokenExpiry", java.time.LocalDateTime.class);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.cart = inits.isInitialized("cart") ? new com.cMall.feedShop.cart.domain.model.QCart(forProperty("cart"), inits.get("cart")) : null;
        this.userPoint = inits.isInitialized("userPoint") ? new QUserPoint(forProperty("userPoint"), inits.get("userPoint")) : null;
        this.userProfile = inits.isInitialized("userProfile") ? new QUserProfile(forProperty("userProfile"), inits.get("userProfile")) : null;
    }

}

