package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.entity.FeedVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedVoteRepository extends JpaRepository<FeedVote, Long> {

    /**
     * 특정 피드에 특정 사용자가 투표했는지 확인
     */
    boolean existsByFeed_IdAndVoter_Id(Long feedId, Long voterId);

    /**
     * 특정 피드에 특정 사용자의 투표 조회
     */
    Optional<FeedVote> findByFeed_IdAndVoter_Id(Long feedId, Long voterId);

    /**
     * 특정 피드의 투표 목록 조회
     */
    List<FeedVote> findByFeed_Id(Long feedId);

    /**
     * 특정 사용자의 투표 목록 조회
     */
    List<FeedVote> findByVoter_Id(Long voterId);

    /**
     * 특정 이벤트의 투표 목록 조회
     */
    List<FeedVote> findByEvent_Id(Long eventId);

    /**
     * 특정 피드의 투표 개수 조회
     */
    long countByFeed_Id(Long feedId);

    /**
     * 특정 이벤트의 투표 개수 조회
     */
    long countByEvent_Id(Long eventId);

    /**
     * 특정 사용자가 특정 이벤트에 투표했는지 확인
     */
    boolean existsByVoter_IdAndEvent_Id(Long voterId, Long eventId);

    /**
     * 특정 이벤트에서 특정 사용자가 투표했는지 확인 (develop 브랜치 호환)
     */
    @Query("select count(v) > 0 from FeedVote v where v.event.id = :eventId and v.voter.id = :userId")
    boolean existsByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * 특정 이벤트의 투표 개수 조회 (develop 브랜치 호환)
     */
    @Query("select count(v) from FeedVote v where v.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);

    /**
     * 특정 사용자가 특정 이벤트에 투표한 피드 조회 (develop 브랜치 호환)
     */
    @Query("select v.feed from FeedVote v where v.event.id = :eventId and v.voter.id = :userId")
    Optional<com.cMall.feedShop.feed.domain.entity.Feed> findVotedFeedByEventAndUser(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
