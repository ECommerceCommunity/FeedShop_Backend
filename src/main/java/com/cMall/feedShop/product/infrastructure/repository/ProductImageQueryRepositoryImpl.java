package com.cMall.feedShop.product.infrastructure.repository;

import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.QProductImage;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.cMall.feedShop.product.domain.model.QProductImage.productImage;

/**
 * ProductImage QueryDSL 구현체
 * 복잡한 조회 로직들을 QueryDSL로 처리하는 클래스
 */
@Repository
@RequiredArgsConstructor
public class ProductImageQueryRepositoryImpl implements ProductImageQueryRepository {

    // QueryDSL을 사용하기 위한 Factory (쿼리 생성 도구)
    private final JPAQueryFactory queryFactory;

    /**
     * 상품 ID 목록에 해당하는 상품 이미지들 중, 각 상품의 첫 번째 이미지만 조회한다.
     * @param productIds 조회할 상품 ID 목록
     * @return 각 상품의 첫 번째 이미지 목록
     */
    @Override
    public List<ProductImage> findFirstImagesByProductIds(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();  // 빈 목록 반환
        }

        return queryFactory
                .selectFrom(productImage)
                .where(
                        productIdIn(productIds)  // 요청한 상품들만
                                .and(isFirstImageOfProduct())  // 각 상품의 첫 번째 이미지만
                )
                .orderBy(productImage.product.productId.asc())  // 상품 ID 순서로 정렬
                .fetch();
    }

    private BooleanExpression productIdIn(Set<Long> productIds) {
        return productIds != null && !productIds.isEmpty()
                ? productImage.product.productId.in(productIds)
                : null;
    }

    private BooleanExpression isFirstImageOfProduct() {
        return productImage.imageId.eq(
                queryFactory
                        .select(productImage.imageId.min())  // 가장 작은 imageId
                        .from(productImage)
                        .where(productImage.product.productId.eq(
                                productImage.product.productId  // 같은 상품의
                        ))
        );
    }
}