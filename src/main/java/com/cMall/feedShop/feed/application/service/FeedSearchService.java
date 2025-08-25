package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedSearchResponseDto;
import com.cMall.feedShop.feed.application.service.FeedLikeService;
import com.cMall.feedShop.feed.application.service.FeedServiceUtils;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.application.service.FeedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 피드 검색 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedSearchService {
    
    private final FeedRepository feedRepository;
    private final FeedMapper feedMapper;
    private final FeedLikeService feedLikeService;
    private final FeedServiceUtils feedServiceUtils;
    
    /**
     * 피드 검색
     * 
     * @param request 검색 요청 조건
     * @param userDetails 사용자 정보 (선택적)
     * @return 검색 결과 (페이징)
     */
    public PaginatedResponse<FeedSearchResponseDto> searchFeeds(FeedSearchRequest request, UserDetails userDetails) {
        log.info("피드 검색 요청 - keyword: {}, authorId: {}, feedType: {}, page: {}, size: {}", 
                request.getKeyword(), request.getAuthorId(), request.getFeedType(), 
                request.getPage(), request.getSize());
        
        // 페이징 설정
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);
        
        // 검색 실행
        Page<Feed> feedPage = feedRepository.findWithSearchConditions(request, pageable);
        
        // Feed 엔티티를 검색용 DTO로 변환
        Page<FeedSearchResponseDto> responsePage = feedPage.map(feedMapper::toFeedSearchResponseDto);
        
        // 사용자별 좋아요 상태 설정
        if (userDetails != null) {
            Long userId = feedServiceUtils.getUserIdFromUserDetails(userDetails);
            responsePage = responsePage.map(dto -> {
                boolean isLiked = feedLikeService.isLikedByUser(dto.getFeedId(), userId);
                
                return FeedSearchResponseDto.builder()
                        .feedId(dto.getFeedId())
                        .title(dto.getTitle())
                        .content(dto.getContent())
                        .feedType(dto.getFeedType())
                        .instagramId(dto.getInstagramId())
                        .createdAt(dto.getCreatedAt())
                        .likeCount(dto.getLikeCount())
                        .commentCount(dto.getCommentCount())
                        .participantVoteCount(dto.getParticipantVoteCount())
                        .userId(dto.getUserId())
                        .userNickname(dto.getUserNickname())
                        .userProfileImg(dto.getUserProfileImg())
                        .productName(dto.getProductName())
                        .eventId(dto.getEventId())
                        .eventTitle(dto.getEventTitle())
                        .hashtags(dto.getHashtags())
                        .imageUrls(dto.getImageUrls())
                        .isLiked(isLiked)
                        .isVoted(false) // 검색에서는 투표 상태 불필요
                        .build();
            });
        }
        
        // PaginatedResponse 생성
        PaginatedResponse<FeedSearchResponseDto> response = PaginatedResponse.<FeedSearchResponseDto>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .hasNext(responsePage.hasNext())
                .hasPrevious(responsePage.hasPrevious())
                .build();
        
        log.info("피드 검색 완료 - 총 {}개, 현재 페이지 {}개", 
                response.getTotalElements(), response.getContent().size());
        
        return response;
    }
}
