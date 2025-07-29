package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.QEvent;
import com.cMall.feedShop.event.domain.QEventDetail;
import com.cMall.feedShop.event.domain.QEventReward;

import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Event> searchEvents(EventListRequestDto requestDto, Pageable pageable) {
        QEvent event = QEvent.event;
        QEventDetail detail = QEventDetail.eventDetail;

        BooleanBuilder builder = new BooleanBuilder();
        
        // 삭제되지 않은 이벤트만 조회
        builder.and(event.deletedAt.isNull());
        
        if (StringUtils.hasText(requestDto.getStatus()) && !"all".equalsIgnoreCase(requestDto.getStatus())) {
            // 상태별 필터링: upcoming, ongoing, completed
            String status = requestDto.getStatus().toLowerCase();
            if ("upcoming".equals(status)) {
                builder.and(event.status.eq(EventStatus.UPCOMING));
            } else if ("ongoing".equals(status)) {
                builder.and(event.status.eq(EventStatus.ONGOING));
            } else if ("completed".equals(status)) {
                builder.and(event.status.eq(EventStatus.ENDED));
            }
        }
        if (StringUtils.hasText(requestDto.getType()) && !"all".equalsIgnoreCase(requestDto.getType())) {
            builder.and(event.type.eq(EventType.valueOf(requestDto.getType().toUpperCase())));
        }
        if (StringUtils.hasText(requestDto.getKeyword())) {
            builder.and(detail.title.containsIgnoreCase(requestDto.getKeyword()));
        }

        // 동적 정렬 처리
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(requestDto, event, detail);

        // 메인 쿼리: Event + Details + Rewards join
        QEventReward reward = QEventReward.eventReward;
        
        List<Event> events = queryFactory
                .selectFrom(event)
                .leftJoin(event.eventDetail, detail).fetchJoin()
                .leftJoin(event.rewards, reward).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 (rewards join으로 인한 중복 제거)
        long total = queryFactory.select(event.countDistinct())
                .from(event)
                .leftJoin(event.eventDetail, detail)
                .leftJoin(event.rewards, reward)
                .where(builder)
                .fetchOne();

        // rewards 매핑: 각 이벤트별로 EventReward를 조회하여 등수별 보상 리스트로 변환
        List<Event> pagedEvents = events;
        
        // 실제 서비스에서는 DTO로 변환하여 반환해야 함
        // (여기서는 Event만 반환, Service에서 toSummaryDto에서 rewards 매핑 구현 필요)
        return new PageImpl<>(pagedEvents, pageable, total);
    }

    @Override
    public Optional<Event> findDetailById(Long id) {
        QEvent event = QEvent.event;
        QEventDetail detail = QEventDetail.eventDetail;
        QEventReward reward = QEventReward.eventReward;

        Event result = queryFactory
                .selectFrom(event)
                .leftJoin(event.eventDetail, detail).fetchJoin()
                .leftJoin(event.rewards, reward).fetchJoin()
                .where(event.id.eq(id)
                        .and(event.deletedAt.isNull())) // 삭제되지 않은 이벤트만 조회
                .fetchOne();
        return Optional.ofNullable(result);
    }

    // sort 파라미터에 따라 동적 정렬 조건 반환
    private OrderSpecifier<?> getOrderSpecifier(EventListRequestDto requestDto, QEvent event, QEventDetail detail) {
        String sort = requestDto.getSort();
        if (sort == null || sort.equalsIgnoreCase("latest")) {
            // 최신순: 생성일 내림차순
            return new OrderSpecifier<>(Order.DESC, event.createdAt);
        } else if (sort.equalsIgnoreCase("upcoming")) {
            // 예정순: 이벤트 시작일 오름차순
            return new OrderSpecifier<>(Order.ASC, detail.eventStartDate);
        } else if (sort.equalsIgnoreCase("past")) {
            // 지난순: 이벤트 종료일 내림차순
            return new OrderSpecifier<>(Order.DESC, detail.eventEndDate);
        }
        // 기본값: 최신순
        return new OrderSpecifier<>(Order.DESC, event.createdAt);
    }
}
