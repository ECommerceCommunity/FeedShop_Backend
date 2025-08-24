package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.CommentCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.CommentListResponseDto;
import com.cMall.feedShop.feed.application.dto.response.CommentResponseDto;
import com.cMall.feedShop.feed.domain.entity.Comment;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.repository.CommentRepository;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FeedRewardEventHandler feedRewardEventHandler;

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentResponseDto createComment(Long feedId, Long userId, CommentCreateRequestDto requestDto) {
        // 피드 존재 확인
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 댓글 생성
        Comment comment = Comment.builder()
                .feed(feed)
                .user(user)
                .content(requestDto.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("댓글 생성 완료 - 피드ID: {}, 사용자ID: {}, 댓글ID: {}", feedId, userId, savedComment.getId());

        // 댓글 작성 리워드 이벤트 생성
        try {
            feedRewardEventHandler.createCommentDailyAchievementEvent(user, feed);
        } catch (Exception e) {
            log.warn("댓글 작성 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}", userId, feedId, e);
            // 리워드 이벤트 생성 실패가 댓글 생성에 영향을 주지 않도록 예외를 던지지 않음
        }

        return CommentResponseDto.from(savedComment);
    }

    /**
     * 피드의 댓글 목록 조회 (페이징) - 페이징과 fetch join 분리
     */
    public CommentListResponseDto getComments(Long feedId, int page, int size) {
        // 피드 존재 확인
        if (!feedRepository.findById(feedId).isPresent()) {
            throw new BusinessException(ErrorCode.FEED_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size);
        
        // 1단계: 페이징된 댓글 ID 목록 조회
        Page<Long> commentIdsPage = commentRepository.findCommentIdsByFeedId(feedId, pageable);
        
        // 2단계: 댓글 ID 목록으로 fetch join하여 댓글 상세 정보 조회
        List<Comment> comments = commentRepository.findByIdsWithUser(commentIdsPage.getContent());
        
        // 3단계: DTO 변환
        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
        
        // 4단계: PaginatedResponse 생성
        PaginatedResponse<CommentResponseDto> pagination = PaginatedResponse.<CommentResponseDto>builder()
                .content(commentDtos)
                .page(page)
                .size(size)
                .totalElements(commentIdsPage.getTotalElements())
                .totalPages(commentIdsPage.getTotalPages())
                .hasNext(commentIdsPage.hasNext())
                .hasPrevious(commentIdsPage.hasPrevious())
                .build();

        return CommentListResponseDto.builder()
                .pagination(pagination)
                .totalComments(commentIdsPage.getTotalElements())
                .build();
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long feedId, Long commentId, Long userId) {
        // 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 피드 일치 확인
        if (!comment.getFeed().getId().equals(feedId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 권한 확인 (댓글 작성자만 삭제 가능)
        if (!comment.isWrittenBy(userId)) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        commentRepository.delete(comment);
        log.info("댓글 삭제 완료 - 피드ID: {}, 댓글ID: {}, 사용자ID: {}", feedId, commentId, userId);
    }
}
