package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 피드의 댓글 목록을 페이징으로 조회 (최신순) - 페이징용
     */
    @Query("select c from Comment c where c.feed.id = :feedId order by c.createdAt desc")
    Page<Comment> findByFeedId(@Param("feedId") Long feedId, Pageable pageable);

    /**
     * 특정 피드의 댓글 ID 목록을 페이징으로 조회 (최신순) - 페이징용
     */
    @Query("select c.id from Comment c where c.feed.id = :feedId order by c.createdAt desc")
    Page<Long> findCommentIdsByFeedId(@Param("feedId") Long feedId, Pageable pageable);

    /**
     * 댓글 ID 목록으로 댓글을 fetch join으로 조회 - N+1 해결용
     */
    @Query("select c from Comment c join fetch c.user u join fetch u.userProfile up where c.id in :commentIds order by c.createdAt desc")
    List<Comment> findByIdsWithUser(@Param("commentIds") List<Long> commentIds);

    /**
     * 특정 피드의 댓글 개수 조회
     */
    @Query("select count(c) from Comment c where c.feed.id = :feedId")
    long countByFeedId(@Param("feedId") Long feedId);

    /**
     * 특정 사용자가 작성한 댓글 ID 목록을 페이징으로 조회 (페이징용)
     */
    @Query("select c.id from Comment c where c.user.id = :userId order by c.createdAt desc")
    Page<Long> findCommentIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 댓글 ID 목록으로 댓글을 fetch join으로 조회 - N+1 해결용
     */
    @Query("select c from Comment c join fetch c.feed f join fetch f.user fu join fetch fu.userProfile fup where c.id in :commentIds order by c.createdAt desc")
    List<Comment> findByIdsWithFeedAndAuthor(@Param("commentIds") List<Long> commentIds);

    /**
     * 특정 사용자가 작성한 댓글 목록 조회 (페이징 없음)
     */
    @Query("select c from Comment c join fetch c.feed f where c.user.id = :userId order by c.createdAt desc")
    List<Comment> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 피드의 댓글 존재 여부 확인
     */
    boolean existsByFeedId(Long feedId);

    /**
     * 특정 사용자가 특정 피드에 작성한 댓글 존재 여부 확인
     */
    boolean existsByFeedIdAndUserId(Long feedId, Long userId);
}
