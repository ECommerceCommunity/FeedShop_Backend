package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedHashtag;
import com.cMall.feedShop.feed.domain.FeedImage;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.event.domain.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedMapper {
    
    /**
     * FeedCreateRequestDto를 Feed 엔티티로 변환
     */
    public Feed toFeed(FeedCreateRequestDto requestDto, OrderItem orderItem, User user, Event event) {
        return Feed.builder()
                .event(event)
                .orderItem(orderItem)
                .user(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .instagramId(requestDto.getInstagramId())
                .build();
    }
    
    /**
     * Feed 엔티티를 FeedCreateResponseDto로 변환
     */
    public FeedCreateResponseDto toFeedCreateResponseDto(Feed feed) {
        return FeedCreateResponseDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .feedType(feed.getFeedType())
                .instagramId(feed.getInstagramId())
                .createdAt(feed.getCreatedAt())
                .userId(feed.getUser().getId())
                .userNickname(getUserNickname(feed.getUser()))
                .orderItemId(getOrderItemId(feed.getOrderItem()))
                .productName(getProductName(feed.getOrderItem()))
                .eventId(feed.getEvent() != null ? feed.getEvent().getId() : null)
                .eventTitle(getEventTitle(feed.getEvent()))
                .hashtags(getHashtags(feed.getHashtags()))
                .imageUrls(getImageUrls(feed.getImages()))
                .build();
    }
    
    /**
     * 사용자 닉네임을 안전하게 가져오기
     */
    private String getUserNickname(User user) {
        if (user == null) return "알 수 없음";
        if (user.getUserProfile() == null) return "알 수 없음";
        return user.getUserProfile().getNickname() != null ? user.getUserProfile().getNickname() : "알 수 없음";
    }
    
    /**
     * 상품명을 안전하게 가져오기
     */
    private String getProductName(OrderItem orderItem) {
        if (orderItem == null) return "알 수 없는 상품";
        if (orderItem.getProductOption() == null) return "알 수 없는 상품";
        if (orderItem.getProductOption().getProduct() == null) return "알 수 없는 상품";
        return orderItem.getProductOption().getProduct().getName() != null ? 
               orderItem.getProductOption().getProduct().getName() : "알 수 없는 상품";
    }
    
    /**
     * 주문 아이템 ID를 안전하게 가져오기
     */
    private Long getOrderItemId(OrderItem orderItem) {
        if (orderItem == null) return null;
        return orderItem.getOrderItemId();
    }
    
    /**
     * 해시태그 목록을 안전하게 가져오기
     */
    private List<String> getHashtags(List<FeedHashtag> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return List.of();
        }
        return hashtags.stream()
                .map(hashtag -> hashtag.getTag())
                .collect(Collectors.toList());
    }
    
    /**
     * 이미지 URL 목록을 안전하게 가져오기
     */
    private List<String> getImageUrls(List<FeedImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .sorted((img1, img2) -> Integer.compare(img1.getSortOrder(), img2.getSortOrder()))
                .map(image -> image.getImageUrl())
                .collect(Collectors.toList());
    }
    
    /**
     * 이벤트 제목을 안전하게 가져오기
     */
    private String getEventTitle(Event event) {
        if (event == null) return null;
        if (event.getEventDetail() == null) return null;
        return event.getEventDetail().getTitle();
    }
} 