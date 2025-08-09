package com.cMall.feedShop.product.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductImage is a Querydsl query type for ProductImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductImage extends EntityPathBase<ProductImage> {

    private static final long serialVersionUID = -864859674L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductImage productImage = new QProductImage("productImage");

    public final NumberPath<Long> imageId = createNumber("imageId", Long.class);

    public final ListPath<com.cMall.feedShop.order.domain.model.OrderItem, com.cMall.feedShop.order.domain.model.QOrderItem> orderItems = this.<com.cMall.feedShop.order.domain.model.OrderItem, com.cMall.feedShop.order.domain.model.QOrderItem>createList("orderItems", com.cMall.feedShop.order.domain.model.OrderItem.class, com.cMall.feedShop.order.domain.model.QOrderItem.class, PathInits.DIRECT2);

    public final QProduct product;

    public final EnumPath<com.cMall.feedShop.product.domain.enums.ImageType> type = createEnum("type", com.cMall.feedShop.product.domain.enums.ImageType.class);

    public final StringPath url = createString("url");

    public QProductImage(String variable) {
        this(ProductImage.class, forVariable(variable), INITS);
    }

    public QProductImage(Path<? extends ProductImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductImage(PathMetadata metadata, PathInits inits) {
        this(ProductImage.class, metadata, inits);
    }

    public QProductImage(Class<? extends ProductImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

