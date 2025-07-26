package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.event.domain.Event;
import org.springframework.stereotype.Component;

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
                .userNickname(feed.getUser().getUserProfile().getNickname())
                .orderItemId(feed.getOrderItem().getOrderItemId())
                .productName(feed.getOrderItem().getProductOption().getProduct().getName())
                .eventId(feed.getEvent() != null ? feed.getEvent().getId() : null)
                .eventTitle(feed.getEvent() != null ? feed.getEvent().getEventDetail().getTitle() : null)
                .hashtags(feed.getHashtags().stream()
                        .map(hashtag -> hashtag.getTag())
                        .collect(Collectors.toList()))
                .imageUrls(feed.getImages().stream()
                        .sorted((img1, img2) -> Integer.compare(img1.getSortOrder(), img2.getSortOrder()))
                        .map(image -> image.getImageUrl())
                        .collect(Collectors.toList()))
                .build();
    }
} 