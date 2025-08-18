package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.MyCommentItemDto;
import com.cMall.feedShop.feed.application.dto.response.MyCommentListResponseDto;
import com.cMall.feedShop.feed.domain.repository.CommentRepository;
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
public class MyCommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * 내가 작성한 댓글 목록 조회 (페이징)
     */
    public MyCommentListResponseDto getMyComments(Long userId, int page, int size) {
        // 사용자 존재 확인
        if (!userRepository.findById(userId).isPresent()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<MyCommentItemDto> commentPage = commentRepository.findByUserIdWithFeedAndAuthor(userId, pageable)
                .map(MyCommentItemDto::from);

        log.info("내 댓글 목록 조회 완료 - 사용자ID: {}, 페이지: {}, 크기: {}, 총 개수: {}", 
                userId, page, size, commentPage.getTotalElements());

        return MyCommentListResponseDto.from(commentPage);
    }
}
