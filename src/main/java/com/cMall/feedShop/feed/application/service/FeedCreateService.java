package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.application.exception.DuplicateFeedException;
import com.cMall.feedShop.feed.application.exception.OrderItemNotFoundException;
import com.cMall.feedShop.feed.application.exception.EventNotAvailableException;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import org.springframework.security.core.userdetails.UserDetails;
import com.cMall.feedShop.user.domain.model.User;
import java.util.List;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedCreateService {
    
    private final FeedRepository feedRepository;
    private final PurchasedItemService purchasedItemService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final FeedMapper feedMapper;
    
    /**
     * 피드 생성
     */
    @Transactional
    public FeedCreateResponseDto createFeed(FeedCreateRequestDto requestDto, UserDetails userDetails) {
        // 1. 사용자 조회
        User user = userRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 2. 구매한 상품 목록 조회 (API 활용)
        List<PurchasedItemInfo> purchasedItems = purchasedItemService.getPurchasedItems(userDetails).getItems();
        
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
        
        // 11. 응답 DTO 변환 및 반환
        return feedMapper.toFeedCreateResponseDto(savedFeed);
    }
    
    /**
     * 이벤트 참여 가능 여부 검증
     */
    private void validateEventAvailability(Event event) {
        // 이벤트가 진행중인지 확인
        if (!event.getStatus().name().equals("ONGOING")) {
            throw new EventNotAvailableException(event.getId(), "진행중이지 않은 이벤트입니다.");
        }
        
        // 이벤트 기간 확인
        if (event.getEventDetail().getEventEndDate().isBefore(java.time.LocalDate.now())) {
            throw new EventNotAvailableException(event.getId(), "이미 종료된 이벤트입니다.");
        }
    }
} 