package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.QReview;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
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
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        return executePagedQuery(conditions, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdOrderByPoints(Long productId, Pageable pageable) {
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        return executePagedQuery(conditions, pageable, review.points.desc(), review.createdAt.desc());
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
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        return queryFactory
                .select(review.count())
                .from(review)
                .where(conditions)
                .fetchOne();
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

    @Override
    public List<Review> findDeletedReviewsByUserId(Long userId) {
        return queryFactory
                .selectFrom(review)
                .where(
                        review.user.id.eq(userId),
                        review.status.eq(ReviewStatus.DELETED)
                )
                .orderBy(review.updatedAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findDeletedReviewsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .selectFrom(review)
                .where(
                        review.status.eq(ReviewStatus.DELETED),
                        review.updatedAt.between(startDate, endDate)
                )
                .orderBy(review.updatedAt.desc())
                .fetch();
    }

    @Override
    public Long countDeletedReviewsByProductId(Long productId) {
        return queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.product.productId.eq(productId),
                        review.status.eq(ReviewStatus.DELETED)
                )
                .fetchOne();
    }

    @Override
    public Long countAllReviewsByProductId(Long productId) {
        return queryFactory
                .select(review.count())
                .from(review)
                .where(review.product.productId.eq(productId))
                .fetchOne();
    }

    @Override
    public Long countDeletedReviewsByUserId(Long userId) {
        return queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.user.id.eq(userId),
                        review.status.eq(ReviewStatus.DELETED)
                )
                .fetchOne();
    }


    // ========== 공통 메서드들 ==========

    /**
     * 활성 리뷰 기본 조건 빌더
     */
    private BooleanBuilder createActiveReviewConditions(Long productId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.product.productId.eq(productId));
        builder.and(review.status.eq(ReviewStatus.ACTIVE));
        builder.and(review.isBlinded.isFalse());
        return builder;
    }

    /**
     * 필터 조건을 BooleanBuilder에 추가
     */
    private BooleanBuilder applyFilters(BooleanBuilder builder, Integer rating, SizeFit sizeFit, Cushion cushion, Stability stability) {
        if (rating != null) {
            builder.and(review.rating.eq(rating));
        }
        if (sizeFit != null) {
            builder.and(review.sizeFit.eq(sizeFit));
        }
        if (cushion != null) {
            builder.and(review.cushion.eq(cushion));
        }
        if (stability != null) {
            builder.and(review.stability.eq(stability));
        }
        return builder;
    }

    /**
     * 공통 페이징 쿼리 실행
     */
    private Page<Review> executePagedQuery(BooleanBuilder conditions, Pageable pageable, OrderSpecifier<?>... orderBy) {
        // 데이터 조회
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(conditions)
                .orderBy(orderBy.length > 0 ? orderBy : new OrderSpecifier[]{review.createdAt.desc()})
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회 (count 쿼리는 0을 반환하므로 null 체크 불필요)
        long total = queryFactory
                .select(review.count())
                .from(review)
                .where(conditions)
                .fetchOne();

        return new PageImpl<>(reviews, pageable, total);
    }

    @Override
    public Long countByUserId(Long userId) {
        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(review.user.id.eq(userId))
                .fetchOne();
        
        return count != null ? count : 0L;
    }

    // ========== 리뷰 필터링 메서드 구현들 ==========

    @Override
    public Page<Review> findActiveReviewsByProductIdAndRating(Long productId, Integer rating, Pageable pageable) {
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        applyFilters(conditions, rating, null, null, null);
        return executePagedQuery(conditions, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdAndSizeFit(Long productId, SizeFit sizeFit, Pageable pageable) {
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        applyFilters(conditions, null, sizeFit, null, null);
        return executePagedQuery(conditions, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdAndCushion(Long productId, Cushion cushion, Pageable pageable) {
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        applyFilters(conditions, null, null, cushion, null);
        return executePagedQuery(conditions, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdAndStability(Long productId, Stability stability, Pageable pageable) {
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        applyFilters(conditions, null, null, null, stability);
        return executePagedQuery(conditions, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdWithFilters(Long productId, Integer rating, SizeFit sizeFit, Cushion cushion, Stability stability, Pageable pageable) {
        BooleanBuilder conditions = createActiveReviewConditions(productId);
        applyFilters(conditions, rating, sizeFit, cushion, stability);
        return executePagedQuery(conditions, pageable);
    }
}