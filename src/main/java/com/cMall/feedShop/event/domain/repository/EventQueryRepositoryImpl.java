package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.QEvent;
import com.cMall.feedShop.event.domain.QEventDetail;
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
        if (StringUtils.hasText(requestDto.getStatus()) && !"all".equalsIgnoreCase(requestDto.getStatus())) {
            builder.and(event.status.eq(EventStatus.valueOf(requestDto.getStatus().toUpperCase())));
        }
        if (StringUtils.hasText(requestDto.getType()) && !"all".equalsIgnoreCase(requestDto.getType())) {
            builder.and(event.type.eq(EventType.valueOf(requestDto.getType().toUpperCase())));
        }
        if (StringUtils.hasText(requestDto.getKeyword())) {
            builder.and(detail.title.containsIgnoreCase(requestDto.getKeyword()));
        }

        // 동적 정렬 처리
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(requestDto, event, detail);

        // 메인 쿼리: Event + Details join, rewards는 서브쿼리로 추출
        List<Event> events = queryFactory
                .selectFrom(event)
                .leftJoin(event.eventDetail, detail).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수
        long total = queryFactory.select(event.count())
                .from(event)
                .leftJoin(event.eventDetail, detail)
                .where(builder)
                .fetchOne();

        // rewards 매핑: 각 이벤트별로 EventReward를 조회하여 등수별 보상 리스트로 변환
        List<Event> pagedEvents = events;
        // 실제 서비스에서는 DTO로 변환하여 반환해야 함
        // (여기서는 Event만 반환, Service에서 toSummaryDto에서 rewards 매핑 구현 필요)
        return new PageImpl<>(pagedEvents, pageable, total);
    }

    // sort 파라미터에 따라 동적 정렬 조건 반환
    private OrderSpecifier<?> getOrderSpecifier(EventListRequestDto requestDto, QEvent event, QEventDetail detail) {
        String sort = requestDto.getSort();
        if (sort == null || sort.equalsIgnoreCase("latest")) {
            // 최신순: 생성일 내림차순
            return new OrderSpecifier<>(Order.DESC, event.createdAt);
        } else if (sort.equalsIgnoreCase("ending")) {
            // 종료임박순: 이벤트 종료일 오름차순
            return new OrderSpecifier<>(Order.ASC, detail.eventEndDate);
        } else if (sort.equalsIgnoreCase("participants")) {
            // 참여자순: 최대 참여자수 내림차순
            return new OrderSpecifier<>(Order.DESC, event.maxParticipants);
        }
        // 기본값: 최신순
        return new OrderSpecifier<>(Order.DESC, event.createdAt);
    }
}
