package com.cMall.feedShop.product.infrastructure.repository;

import com.cMall.feedShop.product.application.dto.request.ProductSearchRequest;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.model.Product;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.cMall.feedShop.product.domain.model.QCategory.category;
import static com.cMall.feedShop.product.domain.model.QProduct.product;
import static com.cMall.feedShop.store.domain.model.QStore.store;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long countWithAllConditions(ProductSearchRequest request) {
        BooleanBuilder whereClause = createAllConditionsWhereClause(request);

        Long count = jpaQueryFactory
                .select(product.count())
                .from(product)
                .where(whereClause)
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    public Page<Product> findWithAllConditions(ProductSearchRequest request, ProductSortType sortType, Pageable pageable) {
        BooleanBuilder whereClause = createAllConditionsWhereClause(request);
        OrderSpecifier<?> orderBy = getOrderSpecifier(sortType);

        List<Product> products = jpaQueryFactory
                .selectFrom(product)
                .leftJoin(product.store, store).fetchJoin()
                .leftJoin(product.category, category).fetchJoin()
                .leftJoin(product.productImages).fetchJoin()
                .where(whereClause)
                .orderBy(orderBy)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(product.count())
                .from(product)
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(products, pageable, totalCount != null ? totalCount : 0);
    }

    /**
     * 모든 조건을 포함한 WHERE 절 생성
     */
    private BooleanBuilder createAllConditionsWhereClause(ProductSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        // 1. 키워드 검색 조건
        if (StringUtils.hasText(request.getKeyword())) {
            builder.and(product.name.containsIgnoreCase(request.getKeyword().trim()));
        }

        // 2. 카테고리 필터링
        if (request.getCategoryId() != null) {
            builder.and(product.category.categoryId.eq(request.getCategoryId()));
        }

        // 3. 최소 가격 필터링
        if (request.getMinPrice() != null) {
            builder.and(product.price.goe(request.getMinPrice()));
        }

        // 4. 최대 가격 필터링
        if (request.getMaxPrice() != null) {
            builder.and(product.price.loe(request.getMaxPrice()));
        }

        // 5. 스토어 필터링
        if (request.getStoreId() != null) {
            builder.and(product.store.storeId.eq(request.getStoreId()));
        }

        return builder;
    }

    private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
        return sortType == ProductSortType.POPULAR
                ? product.wishNumber.desc()
                : product.createdAt.desc();
    }
}
