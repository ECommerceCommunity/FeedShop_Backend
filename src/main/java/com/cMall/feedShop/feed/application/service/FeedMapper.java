package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyFeedListResponseDto;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedHashtag;
import com.cMall.feedShop.feed.domain.entity.FeedImage;
import com.cMall.feedShop.feed.domain.enums.FeedType;
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
    
    // ==================== 피드 목록 조회 관련 매핑 메서드들 ====================
    
    /**
     * Feed 엔티티를 FeedListResponseDto로 변환
     */
    public FeedListResponseDto toFeedListResponseDto(Feed feed) {
        return FeedListResponseDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .feedType(feed.getFeedType())
                .instagramId(feed.getInstagramId())
                .createdAt(feed.getCreatedAt())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .participantVoteCount(feed.getParticipantVoteCount())
                .userId(getUserId(feed.getUser()))
                .userNickname(getUserNickname(feed.getUser()))
                .userProfileImg(null) // TODO: 추후 UserProfile에 profileImg 필드 추가 시 구현
                .userLevel(null) // TODO: 추후 UserProfile에 level 필드 추가 시 구현
                .orderItemId(getOrderItemId(feed.getOrderItem()))
                .productName(getProductName(feed.getOrderItem()))
                .productSize(getProductSize(feed.getOrderItem()))
                .eventId(getEventId(feed.getEvent()))
                .eventTitle(getEventTitle(feed.getEvent()))
                .hashtags(getHashtags(feed.getHashtags()))
                .imageUrls(getImageUrls(feed.getImages()))
                .isLiked(false) // 기본값: 좋아요하지 않음 (서비스에서 실제 상태로 업데이트)
                .isVoted(false) // 기본값: 투표하지 않음 (서비스에서 실제 상태로 업데이트)
                .build();
    }
    
    /**
     * Feed 리스트를 FeedListResponseDto 리스트로 변환
     */
    public List<FeedListResponseDto> toFeedListResponseDtoList(List<Feed> feeds) {
        if (feeds == null || feeds.isEmpty()) {
            return List.of();
        }
        return feeds.stream()
                .map(this::toFeedListResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Feed 엔티티를 MyFeedListResponseDto로 변환
     */
    public MyFeedListResponseDto toMyFeedListResponseDto(Feed feed) {
        return MyFeedListResponseDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .feedType(feed.getFeedType())
                .instagramId(feed.getInstagramId())
                .createdAt(feed.getCreatedAt())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .participantVoteCount(feed.getParticipantVoteCount())
                .userId(getUserId(feed.getUser()))
                .userNickname(getUserNickname(feed.getUser()))
                .userProfileImg(null) // TODO: 추후 UserProfile에 profileImg 필드 추가 시 구현
                .userLevel(null) // TODO: 추후 UserProfile에 level 필드 추가 시 구현
                .orderItemId(getOrderItemId(feed.getOrderItem()))
                .productName(getProductName(feed.getOrderItem()))
                .productSize(getProductSize(feed.getOrderItem()))
                .eventId(getEventId(feed.getEvent()))
                .eventTitle(getEventTitle(feed.getEvent()))
                .hashtags(getHashtags(feed.getHashtags()))
                .imageUrls(getImageUrls(feed.getImages()))
                .isLiked(false) // 기본값: 좋아요하지 않음 (서비스에서 실제 상태로 업데이트)
                .isVoted(false) // 기본값: 투표하지 않음 (서비스에서 실제 상태로 업데이트)
                .build();
    }
    
    /**
     * Feed 리스트를 MyFeedListResponseDto 리스트로 변환
     */
    public List<MyFeedListResponseDto> toMyFeedListResponseDtoList(List<Feed> feeds) {
        if (feeds == null || feeds.isEmpty()) {
            return List.of();
        }
        return feeds.stream()
                .map(this::toMyFeedListResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Feed 엔티티를 FeedDetailResponseDto로 변환
     */
    public FeedDetailResponseDto toFeedDetailResponseDto(Feed feed) {
        return FeedDetailResponseDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .feedType(feed.getFeedType())
                .instagramId(feed.getInstagramId())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .participantVoteCount(feed.getParticipantVoteCount())
                .userId(getUserId(feed.getUser()))
                .userNickname(getUserNickname(feed.getUser()))
                .userProfileImg(null) // TODO: 추후 UserProfile에 profileImg 필드 추가 시 구현
                .userLevel(null) // TODO: 추후 UserProfile에 level 필드 추가 시 구현
                .userGender(null) // TODO: 추후 UserProfile에 gender 필드 추가 시 구현
                .userHeight(null) // TODO: 추후 UserProfile에 height 필드 추가 시 구현
                .orderItemId(getOrderItemId(feed.getOrderItem()))
                .productName(getProductName(feed.getOrderItem()))
                .productSize(getProductSize(feed.getOrderItem()))
                .productImageUrl(getProductImageUrl(feed.getOrderItem()))
                .productId(getProductId(feed.getOrderItem()))
                .eventId(getEventId(feed.getEvent()))
                .eventTitle(getEventTitle(feed.getEvent()))
                .eventDescription(getEventDescription(feed.getEvent()))
                .eventStartDate(getEventStartDate(feed.getEvent()))
                .eventEndDate(getEventEndDate(feed.getEvent()))
                .hashtags(toFeedHashtagDtoList(feed.getHashtags()))
                .images(toFeedImageDtoList(feed.getImages()))
                .comments(toFeedCommentDtoList(feed.getComments()))
                .isLiked(false) // 기본값: 좋아요하지 않음 (서비스에서 실제 상태로 업데이트)
                .isVoted(false) // 기본값: 투표하지 않음 (서비스에서 실제 상태로 업데이트)
                .canVote(feed.getFeedType() == FeedType.EVENT) // 이벤트 피드만 투표 가능
                .build();
    }
    
    // ==================== 추가 헬퍼 메서드들 ====================
    
    /**
     * 사용자 ID를 안전하게 가져오기
     */
    private Long getUserId(User user) {
        return user != null ? user.getId() : null;
    }
    
    /**
     * 사용자 프로필 이미지를 안전하게 가져오기
     * TODO: 추후 UserProfile에 profileImg 필드 추가 시 구현
     */
    private String getUserProfileImg(User user) {
        return null; // TODO: 추후 구현
    }
    
    /**
     * 사용자 레벨을 안전하게 가져오기
     * TODO: 추후 UserProfile에 level 필드 추가 시 구현
     */
    private Integer getUserLevel(User user) {
        return null; // TODO: 추후 구현
    }
    
    /**
     * 사용자 성별을 안전하게 가져오기
     * TODO: 추후 UserProfile에 gender 필드 추가 시 구현
     */
    private String getUserGender(User user) {
        return null; // TODO: 추후 구현
    }
    
    /**
     * 사용자 키를 안전하게 가져오기
     * TODO: 추후 UserProfile에 height 필드 추가 시 구현
     */
    private Integer getUserHeight(User user) {
        return null; // TODO: 추후 구현
    }
    
    /**
     * 상품 사이즈를 안전하게 가져오기
     */
    private Integer getProductSize(OrderItem orderItem) {
        if (orderItem == null || orderItem.getProductOption() == null) return null;
        try {
            return Integer.parseInt(orderItem.getProductOption().getSize().getValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 상품 이미지 URL을 안전하게 가져오기
     */
    private String getProductImageUrl(OrderItem orderItem) {
        if (orderItem == null || orderItem.getProductOption() == null || 
            orderItem.getProductOption().getProduct() == null) return null;
        return orderItem.getProductOption().getProduct().getMainImageUrl();
    }
    
    /**
     * 상품 ID를 안전하게 가져오기
     */
    private Long getProductId(OrderItem orderItem) {
        if (orderItem == null || orderItem.getProductOption() == null || 
            orderItem.getProductOption().getProduct() == null) return null;
        return orderItem.getProductOption().getProduct().getProductId(); // productId 사용
    }
    
    /**
     * 이벤트 ID를 안전하게 가져오기
     */
    private Long getEventId(Event event) {
        return event != null ? event.getId() : null;
    }
    
    /**
     * 이벤트 설명을 안전하게 가져오기
     */
    private String getEventDescription(Event event) {
        if (event == null || event.getEventDetail() == null) return null;
        return event.getEventDetail().getDescription();
    }
    
    /**
     * 이벤트 시작일을 안전하게 가져오기
     */
    private LocalDateTime getEventStartDate(Event event) {
        if (event == null || event.getEventDetail() == null) return null;
        return event.getEventDetail().getEventStartDate().atStartOfDay();
    }
    
    /**
     * 이벤트 종료일을 안전하게 가져오기
     */
    private LocalDateTime getEventEndDate(Event event) {
        if (event == null || event.getEventDetail() == null) return null;
        return event.getEventDetail().getEventEndDate().atStartOfDay();
    }
    
    /**
     * FeedHashtag 리스트를 FeedDetailResponseDto.FeedHashtagDto 리스트로 변환
     */
    private List<FeedDetailResponseDto.FeedHashtagDto> toFeedHashtagDtoList(List<FeedHashtag> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return List.of();
        }
        return hashtags.stream()
                .map(hashtag -> FeedDetailResponseDto.FeedHashtagDto.builder()
                        .tagId(hashtag.getId())
                        .tag(hashtag.getTag())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * FeedImage 리스트를 FeedDetailResponseDto.FeedImageDto 리스트로 변환
     */
    private List<FeedDetailResponseDto.FeedImageDto> toFeedImageDtoList(List<FeedImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .sorted((img1, img2) -> Integer.compare(img1.getSortOrder(), img2.getSortOrder()))
                .map(image -> FeedDetailResponseDto.FeedImageDto.builder()
                        .imageId(image.getId())
                        .imageUrl(image.getImageUrl())
                        .sortOrder(image.getSortOrder())
                        .uploadedAt(image.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Comment 리스트를 FeedDetailResponseDto.FeedCommentDto 리스트로 변환
     */
    private List<FeedDetailResponseDto.FeedCommentDto> toFeedCommentDtoList(List<com.cMall.feedShop.feed.domain.entity.Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }
        return comments.stream()
                .map(comment -> FeedDetailResponseDto.FeedCommentDto.builder()
                        .commentId(comment.getId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                        .userNickname(comment.getUser() != null ? getUserNickname(comment.getUser()) : null)
                        .userProfileImg(comment.getUser() != null ? getUserProfileImg(comment.getUser()) : null)
                        .build())
                .collect(Collectors.toList());
    }
} 