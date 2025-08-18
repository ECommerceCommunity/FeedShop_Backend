package com.cMall.feedShop.product.infrastructure.repository;

import com.cMall.feedShop.product.application.dto.request.ProductSearchRequest;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.model.Product;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.cMall.feedShop.product.domain.model.QCategory.category;
import static com.cMall.feedShop.product.domain.model.QProduct.product;
import static com.cMall.feedShop.product.domain.model.QProductOption.productOption;
import static com.cMall.feedShop.store.domain.model.QStore.store;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long countWithAllConditions(ProductSearchRequest request) {
        BooleanBuilder whereClause = createAllConditionsWhereClause(request);

        Long count = jpaQueryFactory
                .select(product.productId.countDistinct())
                .from(product)
                .leftJoin(product.productOptions, productOption)
                .where(whereClause)
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    public Page<Product> findWithAllConditions(ProductSearchRequest request, ProductSortType sortType, Pageable pageable) {
        BooleanBuilder whereClause = createAllConditionsWhereClause(request);
        OrderSpecifier<?> orderBy = getOrderSpecifier(sortType);

        List<Product> products = jpaQueryFactory
                .selectDistinct(product)
                .from(product)
                .leftJoin(product.store, store).fetchJoin()
                .leftJoin(product.category, category).fetchJoin()
                .leftJoin(product.productImages).fetchJoin()
                .leftJoin(product.productOptions, productOption)
                .where(whereClause)
                .orderBy(orderBy)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(product.productId.countDistinct())
                .from(product)
                .leftJoin(product.productOptions, productOption)
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

        // 6. 색상 필터링 (추가)
        if (request.getColors() != null && !request.getColors().isEmpty()) {
            builder.and(productOption.color.in(request.getColors()));
        }

        // 7. 사이즈 필터링 (추가)
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            builder.and(productOption.size.in(request.getSizes()));
        }

        // 8. 성별 필터링 (추가)
        if (request.getGenders() != null && !request.getGenders().isEmpty()) {
            builder.and(productOption.gender.in(request.getGenders()));
        }

        // 9. 재고 필터링 (추가)
        if (request.getInStockOnly() != null && request.getInStockOnly()) {
            builder.and(productOption.stock.gt(0));
        }

        // 10. 할인 상품 필터링 (추가)
        if (request.getDiscountedOnly() != null && request.getDiscountedOnly()) {
            builder.and(product.discountType.ne(DiscountType.NONE)
                    .or(product.discountValue.gt(BigDecimal.ZERO)));
        }

        return builder;
    }

    /**
     * 정렬 조건 생성
     */
    private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
        switch (sortType) {
            case POPULAR:
                return product.wishNumber.desc();
            case PRICE_ASC:
                return product.price.asc();
            case PRICE_DESC:
                return product.price.desc();
            case NAME_ASC:
                return product.name.asc();
            case NAME_DESC:
                return product.name.desc();
            case DISCOUNT_DESC:
                // 할인율 계산: FIXED_DISCOUNT는 할인금액/원가, RATE_DISCOUNT는 할인율 그대로
                NumberExpression<BigDecimal> discountRate = new CaseBuilder()
                        .when(product.discountType.eq(DiscountType.FIXED_DISCOUNT))
                        .then(product.discountValue.divide(product.price).multiply(100))
                        .when(product.discountType.eq(DiscountType.RATE_DISCOUNT))
                        .then(product.discountValue)
                        .otherwise(BigDecimal.ZERO);
                return discountRate.desc();
            case LATEST:
            default:
                return product.createdAt.desc();
        }
    }
}
