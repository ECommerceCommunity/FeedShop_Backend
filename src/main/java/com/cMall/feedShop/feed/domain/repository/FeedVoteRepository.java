package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    @Query("select v from FeedVote v where v.feed.id = :feedId and v.voter.id = :userId")
    Optional<FeedVote> findByFeedIdAndUserId(@Param("feedId") Long feedId, @Param("userId") Long userId);

    /**
     * 특정 피드의 투표 존재 여부 확인
     */
    boolean existsByFeedId(Long feedId);

    /**
     * 특정 이벤트에서 특정 사용자가 투표했는지 확인
     */
    @Query("select count(v) > 0 from FeedVote v where v.event.id = :eventId and v.voter.id = :userId")
    boolean existsByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * 특정 이벤트의 투표 개수 조회
     */
    @Query("select count(v) from FeedVote v where v.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);

    /**
     * 특정 사용자가 특정 이벤트에 투표한 피드 조회
     */
    @Query("select v.feed from FeedVote v where v.event.id = :eventId and v.voter.id = :userId")
    Optional<Feed> findVotedFeedByEventAndUser(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * 특정 이벤트에서 가장 많은 투표를 받은 피드들 조회 (리워드용)
     */
    @Query("select v.feed, count(v) as voteCount from FeedVote v where v.event.id = :eventId group by v.feed order by voteCount desc")
    List<Object[]> findTopVotedFeedsByEvent(@Param("eventId") Long eventId);

    /**
     * 특정 이벤트에서 가장 많이 투표한 사용자들 조회 (참여 보상용)
     */
    @Query("select v.voter, count(v) as voteCount from FeedVote v where v.event.id = :eventId group by v.voter order by voteCount desc")
    List<Object[]> findTopVotersByEvent(@Param("eventId") Long eventId);

    /**
     * 🔧 개선: 특정 피드의 실제 투표 수를 Feed 엔티티와 동기화
     */
    @Query("select count(v) from FeedVote v where v.feed.id = :feedId")
    long getActualVoteCountByFeedId(@Param("feedId") Long feedId);

    /**
     * 🔧 개선: 모든 피드의 투표 수를 일괄 동기화 (배치 작업용)
     */
    @Query("select v.feed.id, count(v) as voteCount from FeedVote v group by v.feed.id")
    List<Object[]> getAllFeedVoteCounts();
}
