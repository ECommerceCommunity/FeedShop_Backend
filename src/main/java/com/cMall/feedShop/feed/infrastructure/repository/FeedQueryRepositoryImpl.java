package com.cMall.feedShop.feed.infrastructure.repository;

import com.cMall.feedShop.feed.application.dto.request.FeedSearchRequest;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedQueryRepository;
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

import static com.cMall.feedShop.feed.domain.QFeed.feed;
import static com.cMall.feedShop.feed.domain.QFeedHashtag.feedHashtag;
import static com.cMall.feedShop.event.domain.QEvent.event;
import static com.cMall.feedShop.event.domain.QEventDetail.eventDetail;
import static com.cMall.feedShop.order.domain.model.QOrderItem.orderItem;
import static com.cMall.feedShop.product.domain.model.QProduct.product;
import static com.cMall.feedShop.user.domain.model.QUser.user;

/**
 * 피드 검색을 위한 QueryDSL Repository 구현 클래스
 */
@Repository
@RequiredArgsConstructor
public class FeedQueryRepositoryImpl implements FeedQueryRepository {
    
    private final JPAQueryFactory jpaQueryFactory;
    
    @Override
    public long countWithSearchConditions(FeedSearchRequest request) {
        BooleanBuilder whereClause = createSearchConditions(request);
        
        Long count = jpaQueryFactory
                .select(feed.countDistinct())
                .from(feed)
                .leftJoin(feed.hashtags, feedHashtag)
                .leftJoin(feed.orderItem, orderItem)
                .leftJoin(orderItem.productOption, product)
                .leftJoin(feed.event, event)
                .leftJoin(event.eventDetail, eventDetail)
                .where(whereClause)
                .fetchOne();
        
        return count != null ? count : 0;
    }
    
    @Override
    public Page<Feed> findWithSearchConditions(FeedSearchRequest request, Pageable pageable) {
        BooleanBuilder whereClause = createSearchConditions(request);
        OrderSpecifier<?> orderBy = getOrderSpecifier(request.getSort());
        
        // 피드 목록 조회
        List<Feed> feeds = jpaQueryFactory
                .selectDistinct(feed)
                .from(feed)
                .leftJoin(feed.user, user).fetchJoin()
                .leftJoin(feed.hashtags, feedHashtag)
                .leftJoin(feed.orderItem, orderItem)
                .leftJoin(orderItem.productOption, product)
                .leftJoin(feed.event, event)
                .leftJoin(event.eventDetail, eventDetail)
                .where(whereClause)
                .orderBy(orderBy)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        // 전체 개수 조회
        Long totalCount = jpaQueryFactory
                .select(feed.countDistinct())
                .from(feed)
                .leftJoin(feed.hashtags, feedHashtag)
                .leftJoin(feed.orderItem, orderItem)
                .leftJoin(orderItem.productOption, product)
                .leftJoin(feed.event, event)
                .leftJoin(event.eventDetail, eventDetail)
                .where(whereClause)
                .fetchOne();
        
        return new PageImpl<>(feeds, pageable, totalCount != null ? totalCount : 0);
    }
    
    /**
     * 검색 조건을 생성하는 메서드
     */
    private BooleanBuilder createSearchConditions(FeedSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        
        // 1. 키워드 검색 (제목, 내용, 해시태그)
        if (request.hasKeyword()) {
            builder.and(
                feed.title.containsIgnoreCase(request.getKeyword().trim())
                .or(feed.content.containsIgnoreCase(request.getKeyword().trim()))
                .or(feedHashtag.tag.containsIgnoreCase(request.getKeyword().trim()))
            );
        }
        
        // 2. 작성자 검색
        if (request.hasAuthor()) {
            builder.and(feed.user.id.eq(request.getAuthorId()));
        }
        
        // 3. 피드 타입 검색
        if (request.hasFeedType()) {
            builder.and(feed.feedType.eq(request.getFeedType()));
        }
        
        // 4. 날짜 범위 검색
        if (request.getStartDate() != null) {
            builder.and(feed.createdAt.goe(request.getStartDate()));
        }
        if (request.getEndDate() != null) {
            builder.and(feed.createdAt.loe(request.getEndDate()));
        }
        
        // 5. 상품 정보 검색
        if (request.hasProductName()) {
            builder.and(product.name.containsIgnoreCase(request.getProductName().trim()));
        }
        if (request.hasProductId()) {
            builder.and(product.productId.eq(request.getProductId()));
        }
        
        // 6. 이벤트 정보 검색
        if (request.hasEventId()) {
            builder.and(feed.event.id.eq(request.getEventId()));
        }
        if (request.hasEventTitle()) {
            builder.and(eventDetail.title.containsIgnoreCase(request.getEventTitle().trim()));
        }
        
        // 7. 해시태그 검색
        if (request.hasHashtags()) {
            builder.and(feedHashtag.tag.in(request.getHashtags()));
        }
        
        // 8. 삭제되지 않은 피드만
        builder.and(feed.deletedAt.isNull());
        
        return builder;
    }
    
    /**
     * 정렬 조건을 생성하는 메서드
     */
    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (sort == null) {
            return feed.createdAt.desc(); // 기본값: 최신순
        }
        
        switch (sort.toLowerCase()) {
            case "popular":
                return feed.likeCount.desc();
            case "comments":
                return feed.commentCount.desc();
            case "votes":
                return feed.participantVoteCount.desc();
            case "latest":
            default:
                return feed.createdAt.desc();
        }
    }
}
