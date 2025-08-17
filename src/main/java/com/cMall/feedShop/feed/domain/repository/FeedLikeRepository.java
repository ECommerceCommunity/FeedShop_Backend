package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.FeedLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    boolean existsByFeed_IdAndUser_Id(Long feedId, Long userId);

    void deleteByFeed_IdAndUser_Id(Long feedId, Long userId);

    long countByFeed_Id(Long feedId);

    @Query("select fl from FeedLike fl join fetch fl.user u where fl.feed.id = :feedId")
    Page<FeedLike> findByFeedIdWithUser(@Param("feedId") Long feedId, Pageable pageable);
    
    /**
     * 사용자별 좋아요 피드 목록 조회 (페이징)
     * 피드 정보와 함께 좋아요 시간을 포함하여 조회
     */
    @Query("select fl from FeedLike fl join fetch fl.feed f join fetch f.user u where fl.user.id = :userId order by fl.createdAt desc")
    Page<FeedLike> findByUserIdWithFeed(@Param("userId") Long userId, Pageable pageable);

    @Query("select fl.feed.id from FeedLike fl where fl.user.id = :userId")
    List<Long> findFeedIdsByUserId(@Param("userId") Long userId);
}
