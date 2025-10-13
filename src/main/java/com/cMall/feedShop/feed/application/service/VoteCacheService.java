package com.cMall.feedShop.feed.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

/**
 * 투표 수 Redis 캐싱 서비스
 * 
 * <p>투표 수를 Redis에 캐싱하여 빠른 조회 성능을 제공합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoteCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String VOTE_COUNT_PREFIX = "vote:feed:";
    private static final String EVENT_VOTE_COUNT_PREFIX = "vote:event:";
    private static final String VOTE_HISTORY_PREFIX = "vote:history:";
    private static final Duration VOTE_COUNT_TTL = Duration.ofMinutes(30);
    private static final Duration EVENT_VOTE_TTL = Duration.ofHours(1);

    /**
     * 피드 투표 수 업데이트
     * 
     * @param feedId 피드 ID
     * @param count 투표 수
     */
    public void updateVoteCount(Long feedId, int count) {
        String key = VOTE_COUNT_PREFIX + feedId;
        redisTemplate.opsForValue().set(key, count, VOTE_COUNT_TTL);
        
        log.debug("피드 투표 수 캐시 업데이트 - feedId: {}, count: {}", feedId, count);
    }

    /**
     * 피드 투표 수 증가
     * 
     * @param feedId 피드 ID
     * @return 증가 후 투표 수
     */
    public Long incrementVoteCount(Long feedId) {
        String key = VOTE_COUNT_PREFIX + feedId;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, VOTE_COUNT_TTL);
        
        log.debug("피드 투표 수 증가 - feedId: {}, count: {}", feedId, count);
        return count;
    }

    /**
     * 피드 투표 수 감소
     * 
     * @param feedId 피드 ID
     * @return 감소 후 투표 수
     */
    public Long decrementVoteCount(Long feedId) {
        String key = VOTE_COUNT_PREFIX + feedId;
        Long count = redisTemplate.opsForValue().decrement(key);
        
        // 음수가 되지 않도록 처리
        if (count < 0) {
            count = 0L;
            redisTemplate.opsForValue().set(key, 0, VOTE_COUNT_TTL);
        } else {
            redisTemplate.expire(key, VOTE_COUNT_TTL);
        }
        
        log.debug("피드 투표 수 감소 - feedId: {}, count: {}", feedId, count);
        return count;
    }

    /**
     * 피드 투표 수 조회
     * 
     * @param feedId 피드 ID
     * @return 투표 수 (캐시에 없으면 null)
     */
    public Integer getVoteCount(Long feedId) {
        String key = VOTE_COUNT_PREFIX + feedId;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        
        return null;
    }

    /**
     * 이벤트별 총 투표 수 증가
     * 
     * @param eventId 이벤트 ID
     * @return 증가 후 총 투표 수
     */
    public Long incrementEventVoteCount(Long eventId) {
        String key = EVENT_VOTE_COUNT_PREFIX + eventId;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, EVENT_VOTE_TTL);
        
        log.debug("이벤트 투표 수 증가 - eventId: {}, count: {}", eventId, count);
        return count;
    }

    /**
     * 이벤트별 총 투표 수 감소
     * 
     * @param eventId 이벤트 ID
     * @return 감소 후 총 투표 수
     */
    public Long decrementEventVoteCount(Long eventId) {
        String key = EVENT_VOTE_COUNT_PREFIX + eventId;
        Long count = redisTemplate.opsForValue().decrement(key);
        
        if (count < 0) {
            count = 0L;
            redisTemplate.opsForValue().set(key, 0, EVENT_VOTE_TTL);
        } else {
            redisTemplate.expire(key, EVENT_VOTE_TTL);
        }
        
        log.debug("이벤트 투표 수 감소 - eventId: {}, count: {}", eventId, count);
        return count;
    }

    /**
     * 이벤트별 총 투표 수 조회
     * 
     * @param eventId 이벤트 ID
     * @return 총 투표 수
     */
    public Integer getEventVoteCount(Long eventId) {
        String key = EVENT_VOTE_COUNT_PREFIX + eventId;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        
        return null;
    }

    /**
     * 사용자 투표 기록 캐싱
     * 
     * @param userId 사용자 ID
     * @param feedId 피드 ID
     */
    public void cacheUserVote(Long userId, Long feedId) {
        String key = VOTE_HISTORY_PREFIX + userId;
        redisTemplate.opsForSet().add(key, feedId.toString());
        redisTemplate.expire(key, Duration.ofDays(1));
        
        log.debug("사용자 투표 기록 캐시 - userId: {}, feedId: {}", userId, feedId);
    }

    /**
     * 사용자 투표 기록 삭제
     * 
     * @param userId 사용자 ID
     * @param feedId 피드 ID
     */
    public void removeUserVote(Long userId, Long feedId) {
        String key = VOTE_HISTORY_PREFIX + userId;
        redisTemplate.opsForSet().remove(key, feedId.toString());
        
        log.debug("사용자 투표 기록 삭제 - userId: {}, feedId: {}", userId, feedId);
    }

    /**
     * 사용자가 특정 피드에 투표했는지 확인
     * 
     * @param userId 사용자 ID
     * @param feedId 피드 ID
     * @return 투표 여부
     */
    public boolean hasUserVoted(Long userId, Long feedId) {
        String key = VOTE_HISTORY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, feedId.toString()));
    }

    /**
     * 피드 투표 수 캐시 삭제
     * 
     * @param feedId 피드 ID
     */
    public void evictVoteCount(Long feedId) {
        String key = VOTE_COUNT_PREFIX + feedId;
        redisTemplate.delete(key);
        
        log.debug("피드 투표 수 캐시 삭제 - feedId: {}", feedId);
    }

    /**
     * 이벤트 관련 모든 투표 수 캐시 삭제
     * 
     * @param eventId 이벤트 ID
     */
    public void evictEventVoteCache(Long eventId) {
        String eventKey = EVENT_VOTE_COUNT_PREFIX + eventId;
        redisTemplate.delete(eventKey);
        
        // 이벤트 관련 모든 피드 투표 수도 삭제
        Set<String> keys = redisTemplate.keys(VOTE_COUNT_PREFIX + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        
        log.debug("이벤트 투표 수 캐시 삭제 - eventId: {}", eventId);
    }

    /**
     * 모든 투표 수 캐시 삭제 (관리자용)
     */
    public void evictAllVoteCache() {
        Set<String> voteKeys = redisTemplate.keys(VOTE_COUNT_PREFIX + "*");
        Set<String> eventKeys = redisTemplate.keys(EVENT_VOTE_COUNT_PREFIX + "*");
        Set<String> historyKeys = redisTemplate.keys(VOTE_HISTORY_PREFIX + "*");
        
        if (voteKeys != null) redisTemplate.delete(voteKeys);
        if (eventKeys != null) redisTemplate.delete(eventKeys);
        if (historyKeys != null) redisTemplate.delete(historyKeys);
        
        log.info("모든 투표 수 캐시 삭제 완료");
    }
}
