package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.entity.FeedVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
}
