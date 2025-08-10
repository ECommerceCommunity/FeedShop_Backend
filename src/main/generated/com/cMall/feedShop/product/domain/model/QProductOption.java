package com.cMall.feedShop.product.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductOption is a Querydsl query type for ProductOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductOption extends EntityPathBase<ProductOption> {

    private static final long serialVersionUID = -865732278L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductOption productOption = new QProductOption("productOption");

    public final EnumPath<com.cMall.feedShop.product.domain.enums.Color> color = createEnum("color", com.cMall.feedShop.product.domain.enums.Color.class);

    public final EnumPath<com.cMall.feedShop.product.domain.enums.Gender> gender = createEnum("gender", com.cMall.feedShop.product.domain.enums.Gender.class);

    public final NumberPath<Long> optionId = createNumber("optionId", Long.class);

    public final ListPath<com.cMall.feedShop.order.domain.model.OrderItem, com.cMall.feedShop.order.domain.model.QOrderItem> orderItems = this.<com.cMall.feedShop.order.domain.model.OrderItem, com.cMall.feedShop.order.domain.model.QOrderItem>createList("orderItems", com.cMall.feedShop.order.domain.model.OrderItem.class, com.cMall.feedShop.order.domain.model.QOrderItem.class, PathInits.DIRECT2);

    public final QProduct product;

    public final EnumPath<com.cMall.feedShop.product.domain.enums.Size> size = createEnum("size", com.cMall.feedShop.product.domain.enums.Size.class);

    public final NumberPath<Integer> stock = createNumber("stock", Integer.class);

    public QProductOption(String variable) {
        this(ProductOption.class, forVariable(variable), INITS);
    }

    public QProductOption(Path<? extends ProductOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductOption(PathMetadata metadata, PathInits inits) {
        this(ProductOption.class, metadata, inits);
    }

    public QProductOption(Class<? extends ProductOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

