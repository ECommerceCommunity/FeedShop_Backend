package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.QReview;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QReview review = QReview.review;

    @Override
    public Page<Review> findActiveReviewsByProductId(Long productId, Pageable pageable) {
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .orderBy(review.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .fetchOne();

        return new PageImpl<>(reviews, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdOrderByPoints(Long productId, Pageable pageable) {
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .orderBy(review.points.desc(), review.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .fetchOne();

        return new PageImpl<>(reviews, pageable, total != null ? total : 0L);
    }

    @Override
    public Double findAverageRatingByProductId(Long productId) {
        return queryFactory
                .select(review.rating.avg())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .fetchOne();
    }

    @Override
    public Long countActiveReviewsByProductId(Long productId) {
        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public Map<Cushion, Long> getCushionDistributionByProductId(Long productId) {
        List<Tuple> results = queryFactory
                .select(review.cushion, review.count())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .groupBy(review.cushion)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(review.cushion),
                        tuple -> tuple.get(review.count())
                ));
    }

    @Override
    public Map<SizeFit, Long> getSizeFitDistributionByProductId(Long productId) {
        List<Tuple> results = queryFactory
                .select(review.sizeFit, review.count())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .groupBy(review.sizeFit)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(review.sizeFit),
                        tuple -> tuple.get(review.count())
                ));
    }

    @Override
    public Map<Stability, Long> getStabilityDistributionByProductId(Long productId) {
        List<Tuple> results = queryFactory
                .select(review.stability, review.count())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .groupBy(review.stability)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(review.stability),
                        tuple -> tuple.get(review.count())
                ));
    }


    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.user.id.eq(userId),
                        review.product.productId.eq(productId)
                )
                .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public boolean existsActiveReviewByUserIdAndProductId(Long userId, Long productId) {
        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.user.id.eq(userId),
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.ACTIVE),
                        review.isBlinded.isFalse()
                )
                .fetchOne();

        return count != null && count > 0;
    }

}