package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.MyCommentListResponseDto;
import com.cMall.feedShop.feed.domain.Comment;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.CommentRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MyCommentService 테스트")
class MyCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @Mock
    private UserProfile userProfile;

    @Mock
    private Feed feed;

    @Mock
    private Comment comment;

    @InjectMocks
    private MyCommentService myCommentService;

    @BeforeEach
    void setUp() {
        // 기본 Mock 설정 - 실제로 사용되는 것만 설정
    }

    @Test
    @DisplayName("내 댓글 목록 조회 성공")
    void getMyComments_success() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);

        List<Comment> comments = List.of(comment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, 1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findByUserIdWithFeedAndAuthor(userId, pageable)).thenReturn(commentPage);
        
        // MyCommentItemDto.from()에서 사용하는 Mock 설정
        when(user.getUserProfile()).thenReturn(userProfile);
        when(userProfile.getNickname()).thenReturn("테스트유저");
        when(userProfile.getProfileImageUrl()).thenReturn("test-profile.jpg");
        
        when(feed.getId()).thenReturn(1L);
        when(feed.getTitle()).thenReturn("테스트 피드");
        when(feed.getFeedType()).thenReturn(com.cMall.feedShop.feed.domain.FeedType.DAILY);
        when(feed.getUser()).thenReturn(user);
        
        when(comment.getId()).thenReturn(1L);
        when(comment.getContent()).thenReturn("테스트 댓글");
        when(comment.getFeed()).thenReturn(feed);
        when(comment.getCreatedAt()).thenReturn(LocalDateTime.now());

        // when
        MyCommentListResponseDto result = myCommentService.getMyComments(userId, page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCommentId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("테스트 댓글");
        assertThat(result.getContent().get(0).getFeedId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getFeedTitle()).isEqualTo("테스트 피드");
        assertThat(result.getContent().get(0).getAuthorNickname()).isEqualTo("테스트유저");
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);

        verify(userRepository).findById(userId);
        verify(commentRepository).findByUserIdWithFeedAndAuthor(userId, pageable);
    }

    @Test
    @DisplayName("내 댓글 목록 조회 실패 - 사용자가 존재하지 않음")
    void getMyComments_userNotFound() {
        // given
        Long userId = 999L;
        int page = 0;
        int size = 20;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myCommentService.getMyComments(userId, page, size))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(userId);
        verify(commentRepository, never()).findByUserIdWithFeedAndAuthor(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("내 댓글 목록 조회 - 빈 페이지")
    void getMyComments_emptyPage() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);

        Page<Comment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findByUserIdWithFeedAndAuthor(userId, pageable)).thenReturn(emptyPage);

        // when
        MyCommentListResponseDto result = myCommentService.getMyComments(userId, page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);

        verify(userRepository).findById(userId);
        verify(commentRepository).findByUserIdWithFeedAndAuthor(userId, pageable);
    }
}
