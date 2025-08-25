package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.entity.FeedLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    /**
     * 특정 피드에 특정 사용자가 좋아요했는지 확인
     */
    boolean existsByFeed_IdAndUser_Id(Long feedId, Long userId);

    /**
     * 특정 피드에 특정 사용자의 좋아요 조회
     */
    Optional<FeedLike> findByFeed_IdAndUser_Id(Long feedId, Long userId);

    /**
     * 특정 피드에 특정 사용자의 좋아요 삭제
     */
    void deleteByFeed_IdAndUser_Id(Long feedId, Long userId);

    /**
     * 특정 피드의 좋아요 목록 조회
     */
    List<FeedLike> findByFeed_Id(Long feedId);

    /**
     * 특정 사용자의 좋아요 목록 조회 (페이징)
     */
    Page<FeedLike> findByUser_Id(Long userId, Pageable pageable);

    /**
     * 특정 피드의 좋아요 개수 조회
     */
    long countByFeed_Id(Long feedId);

    /**
     * 특정 사용자의 좋아요 개수 조회
     */
    long countByUser_Id(Long userId);

    /**
     * 여러 피드에 대한 사용자의 좋아요 상태 일괄 조회
     * 성능 개선을 위한 일괄 조회 메서드
     */
    @Query("SELECT fl.feed.id FROM FeedLike fl WHERE fl.feed.id IN :feedIds AND fl.user.id = :userId")
    List<Long> findLikedFeedIdsByFeedIdsAndUserId(@Param("feedIds") List<Long> feedIds, @Param("userId") Long userId);
}
