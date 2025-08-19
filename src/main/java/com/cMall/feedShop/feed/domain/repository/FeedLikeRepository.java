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

    @Query("select fl.feed.id from FeedLike fl where fl.user.id = :userId")
    List<Long> findFeedIdsByUserId(@Param("userId") Long userId);
}
