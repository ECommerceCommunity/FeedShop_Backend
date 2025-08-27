package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.application.exception.OrderItemNotFoundException;
import com.cMall.feedShop.feed.application.exception.EventNotAvailableException;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.user.domain.model.User;
import java.util.List;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.event.domain.EventParticipant;
import com.cMall.feedShop.event.domain.repository.EventParticipantRepository;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedCreateService {
    
    private final FeedRepository feedRepository;
    private final PurchasedItemService purchasedItemService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final FeedMapper feedMapper;
    private final FeedRewardEventHandler feedRewardEventHandler;
    
    /**
     * 피드 생성
     */
    @Transactional
    public FeedCreateResponseDto createFeed(FeedCreateRequestDto requestDto, String loginId) {
        // 1. 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 2. 구매한 상품 목록 조회 (API 활용)
        List<PurchasedItemInfo> purchasedItems = purchasedItemService.getPurchasedItems(user.getLoginId()).getItems();
        
        // 3. 해당 주문 상품이 구매 목록에 있는지 검증
        PurchasedItemInfo purchasedItem = purchasedItems.stream()
                .filter(item -> item.getOrderItemId().equals(requestDto.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(requestDto.getOrderItemId()));
        
        // 4. OrderItem 엔티티 조회 (API 검증 후)
        // Order를 조회하고 그 안의 orderItems에서 특정 OrderItem을 찾음
        OrderItem orderItem = orderRepository.findAll().stream()
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getOrderItemId().equals(requestDto.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(requestDto.getOrderItemId()));
        
        // 5. 중복 피드 작성 검증 (제거 - 여러 피드 생성 허용)
        // if (feedRepository.existsByOrderItemIdAndUserId(requestDto.getOrderItemId(), user.getId())) {
        //     throw new DuplicateFeedException(requestDto.getOrderItemId(), user.getId());
        // }
        
        // 6. 이벤트 조회 및 검증 (이벤트 참여 시)
        Event event = null;
        if (requestDto.getEventId() != null) {
            event = eventRepository.findById(requestDto.getEventId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
            
            // 이벤트 참여 가능 여부 검증
            validateEventAvailability(event);
            
            // 이미 해당 이벤트에 참여했는지 확인
            if (eventParticipantRepository.existsByEventIdAndUserId(event.getId(), user.getId())) {
                throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED_IN_EVENT);
            }
        }
        
        // 7. 피드 생성
        Feed feed = feedMapper.toFeed(requestDto, orderItem, user, event);
        
        // 8. 해시태그 추가 (있는 경우)
        if (requestDto.getHashtags() != null && !requestDto.getHashtags().isEmpty()) {
            feed.addHashtags(requestDto.getHashtags());
        }
        
        // 9. 이미지 추가 (있는 경우)
        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            feed.addImages(requestDto.getImageUrls());
        }
        
        // 10. 피드 저장
        Feed savedFeed = feedRepository.save(feed);
        
        // 11. 피드 생성 리워드 이벤트 생성
        try {
            feedRewardEventHandler.createFeedCreationEvent(user, savedFeed);
            
            // 이벤트 피드인 경우 이벤트 참여 리워드 이벤트도 생성
            if (event != null) {
                feedRewardEventHandler.createEventFeedParticipationEvent(user, savedFeed, event.getId());
            }
            
            // 다양한 상품 피드인 경우 관련 리워드 이벤트 생성
            // (현재는 단일 OrderItem만 지원하므로 향후 확장 시 구현)
            
        } catch (Exception e) {
            log.warn("피드 생성 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}", 
                    user.getId(), savedFeed.getId(), e);
            // 리워드 이벤트 생성 실패가 피드 생성에 영향을 주지 않도록 예외를 던지지 않음
        }
        
        // 12. 이벤트 참여 피드인 경우 EventParticipant 자동 생성
        if (event != null) {
            createEventParticipant(event, user, savedFeed);
        }
        
        // 13. 응답 DTO 변환 및 반환
        return feedMapper.toFeedCreateResponseDto(savedFeed);
    }
    
    /**
     * 이벤트 참여자 생성
     */
    private void createEventParticipant(Event event, User user, Feed feed) {
        EventParticipant eventParticipant = EventParticipant.builder()
                .event(event)
                .user(user)
                .feed(feed)
                .metadata(generateEventParticipantMetadata(event, feed))
                .build();
        
        eventParticipantRepository.save(eventParticipant);
    }
    
    /**
     * 이벤트 참여자 메타데이터 생성
     */
    private String generateEventParticipantMetadata(Event event, Feed feed) {
        // 이벤트 타입에 따라 다른 메타데이터 생성
        switch (event.getType()) {
            case BATTLE:
                // 배틀 이벤트는 나중에 매칭 시 메타데이터 업데이트
                return EventParticipant.createBattleMetadata(null, null);
            case RANKING:
                // 랭킹 이벤트는 초기 랭킹 0으로 설정
                return EventParticipant.createRankingMetadata(0, 0L);
            default:
                // 기본 메타데이터
                return String.format("{\"eventType\": \"%s\", \"feedId\": %d}", 
                        event.getType(), feed.getId());
        }
    }
    
    /**
     * 이벤트 참여 가능 여부 검증
     */
    private void validateEventAvailability(Event event) {
        // 실시간으로 계산된 이벤트 상태 확인
        EventStatus calculatedStatus = event.calculateStatus();
        if (calculatedStatus != EventStatus.ONGOING) {
            throw new EventNotAvailableException(event.getId(), 
                String.format("진행중이지 않은 이벤트입니다. 현재 상태: %s", calculatedStatus));
        }
        
        // 이벤트 상세 정보가 있는지 확인
        if (event.getEventDetail() == null) {
            throw new EventNotAvailableException(event.getId(), "이벤트 상세 정보가 없습니다.");
        }
        
        // 이벤트 기간 확인 (추가 검증)
        if (event.getEventDetail().getEventEndDate() == null) {
            throw new EventNotAvailableException(event.getId(), "이벤트 종료일이 설정되지 않았습니다.");
        }
        
        if (event.getEventDetail().getEventEndDate().isBefore(java.time.LocalDate.now())) {
            throw new EventNotAvailableException(event.getId(), "이미 종료된 이벤트입니다.");
        }
    }
} 