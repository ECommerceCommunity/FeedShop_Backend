package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.CommentCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.CommentListResponseDto;
import com.cMall.feedShop.feed.application.dto.response.CommentResponseDto;
import com.cMall.feedShop.feed.domain.Comment;
import com.cMall.feedShop.feed.domain.Feed;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

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

        return CommentResponseDto.from(savedComment);
    }

    /**
     * 피드의 댓글 목록 조회 (페이징)
     */
    public CommentListResponseDto getComments(Long feedId, int page, int size) {
        // 피드 존재 확인
        if (!feedRepository.findById(feedId).isPresent()) {
            throw new BusinessException(ErrorCode.FEED_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentPage = commentRepository.findByFeedIdWithUser(feedId, pageable);
        long totalComments = commentRepository.countByFeedId(feedId);

        // DTO 변환
        Page<CommentResponseDto> responseDtoPage = commentPage.map(CommentResponseDto::from);

        // PaginatedResponse 생성
        PaginatedResponse<CommentResponseDto> pagination = PaginatedResponse.<CommentResponseDto>builder()
                .content(responseDtoPage.getContent())
                .page(page)
                .size(size)
                .totalElements(responseDtoPage.getTotalElements())
                .totalPages(responseDtoPage.getTotalPages())
                .hasNext(responseDtoPage.hasNext())
                .hasPrevious(responseDtoPage.hasPrevious())
                .build();

        return CommentListResponseDto.builder()
                .pagination(pagination)
                .totalComments(totalComments)
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
