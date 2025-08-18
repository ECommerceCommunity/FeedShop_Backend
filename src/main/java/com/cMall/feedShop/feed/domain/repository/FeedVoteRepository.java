package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.FeedVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedVoteRepository extends JpaRepository<FeedVote, Long> {

    /**
     * 특정 피드의 투표 개수 조회
     */
    @Query("select count(v) from FeedVote v where v.feed.id = :feedId")
    long countByFeedId(@Param("feedId") Long feedId);

    /**
     * 특정 사용자가 특정 피드에 투표했는지 확인
     */
    @Query("select v from FeedVote v where v.feed.id = :feedId and v.user.id = :userId")
    Optional<FeedVote> findByFeedIdAndUserId(@Param("feedId") Long feedId, @Param("userId") Long userId);

    /**
     * 특정 사용자가 특정 피드에 투표했는지 존재 여부 확인
     */
    boolean existsByFeedIdAndUserId(Long feedId, Long userId);

    /**
     * 특정 피드의 투표 존재 여부 확인
     */
    boolean existsByFeedId(Long feedId);
}
