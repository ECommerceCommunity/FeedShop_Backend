package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedVote;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedVoteService {

    private final FeedVoteRepository feedVoteRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    /**
     * 피드 투표
     */
    @Transactional
    public FeedVoteResponseDto voteFeed(Long feedId, Long userId) {
        // 피드 존재 확인
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이벤트 피드인지 확인
        if (feed.getFeedType() != com.cMall.feedShop.feed.domain.FeedType.EVENT) {
            throw new BusinessException(ErrorCode.FEED_ACCESS_DENIED);
        }

        // 같은 이벤트에서 이미 다른 피드에 투표했는지 확인
        if (feedVoteRepository.existsByEventIdAndUserId(feed.getEvent().getId(), userId)) {
            log.info("이미 해당 이벤트에 투표함 - 이벤트ID: {}, 사용자ID: {}", feed.getEvent().getId(), userId);
            // 🔧 개선: Feed 엔티티의 participantVoteCount 반환
            return FeedVoteResponseDto.success(false, feed.getParticipantVoteCount());
        }

        // 투표 생성
        FeedVote vote = FeedVote.builder()
                .event(feed.getEvent())
                .feed(feed)
                .voter(user)
                .build();

        feedVoteRepository.save(vote);
        
        // 🔧 개선: Feed 엔티티의 투표 수 증가
        feed.incrementVoteCount();
        
        log.info("피드 투표 완료 - 피드ID: {}, 사용자ID: {}, 투표개수: {}", feedId, userId, feed.getParticipantVoteCount());

        return FeedVoteResponseDto.success(true, feed.getParticipantVoteCount());
    }

    /**
     * 피드의 투표 개수 조회
     */
    public int getVoteCount(Long feedId) {
        // 피드 존재 확인
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        // 🔧 개선: Feed 엔티티의 participantVoteCount 반환
        return feed.getParticipantVoteCount();
    }

    /**
     * 사용자가 특정 피드의 이벤트에 투표했는지 확인
     */
    public boolean hasVoted(Long feedId, Long userId) {
        try {
            // 피드 존재 확인
            Feed feed = feedRepository.findById(feedId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

            // 사용자 존재 확인
            if (!userRepository.findById(userId).isPresent()) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            // 이벤트가 있는 경우에만 이벤트별 투표 확인
            if (feed.getEvent() != null) {
                return feedVoteRepository.existsByEventIdAndUserId(feed.getEvent().getId(), userId);
            }
            
            return false;
        } catch (Exception e) {
            log.error("투표 여부 확인 중 오류 발생 - 피드ID: {}, 사용자ID: {}", feedId, userId, e);
            // 테이블이 존재하지 않는 경우 false 반환
            return false;
        }
    }

    /**
     * 🔧 개선: 특정 피드의 투표 수를 Feed 엔티티와 동기화
     */
    @Transactional
    public void syncVoteCount(Long feedId) {
        try {
            Feed feed = feedRepository.findById(feedId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));
            
            long actualVoteCount = feedVoteRepository.countByFeedId(feedId);
            int currentVoteCount = feed.getParticipantVoteCount();
            
            // 현재 Feed 엔티티의 투표 수와 실제 투표 수가 다르면 동기화
            if (currentVoteCount != (int) actualVoteCount) {
                log.info("투표 수 동기화 - 피드ID: {}, 기존: {}, 실제: {}", 
                        feedId, currentVoteCount, actualVoteCount);
                
                // 🔧 개선: 무한 루프 방지를 위한 안전한 동기화
                int difference = (int) actualVoteCount - currentVoteCount;
                
                if (difference > 0) {
                    // 증가가 필요한 경우
                    for (int i = 0; i < difference; i++) {
                        feed.incrementVoteCount();
                    }
                } else if (difference < 0) {
                    // 감소가 필요한 경우
                    for (int i = 0; i < Math.abs(difference); i++) {
                        feed.decrementVoteCount();
                    }
                }
                
                log.info("투표 수 동기화 완료 - 피드ID: {}, 최종: {}", feedId, feed.getParticipantVoteCount());
            }
        } catch (Exception e) {
            log.error("투표 수 동기화 중 오류 발생 - 피드ID: {}", feedId, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 🔧 개선: 모든 피드의 투표 수를 일괄 동기화 (배치 작업용)
     */
    @Transactional
    public void syncAllVoteCounts() {
        try {
            List<Object[]> voteCounts = feedVoteRepository.getAllFeedVoteCounts();
            int processedCount = 0;
            int syncedCount = 0;
            
            for (Object[] result : voteCounts) {
                Long feedId = (Long) result[0];
                Long actualVoteCount = (Long) result[1];
                
                Feed feed = feedRepository.findById(feedId).orElse(null);
                if (feed != null) {
                    int currentVoteCount = feed.getParticipantVoteCount();
                    
                    if (currentVoteCount != actualVoteCount.intValue()) {
                        log.info("배치 투표 수 동기화 - 피드ID: {}, 기존: {}, 실제: {}", 
                                feedId, currentVoteCount, actualVoteCount);
                        
                        // 🔧 개선: 무한 루프 방지를 위한 안전한 동기화
                        int difference = actualVoteCount.intValue() - currentVoteCount;
                        
                        if (difference > 0) {
                            // 증가가 필요한 경우
                            for (int i = 0; i < difference; i++) {
                                feed.incrementVoteCount();
                            }
                        } else if (difference < 0) {
                            // 감소가 필요한 경우
                            for (int i = 0; i < Math.abs(difference); i++) {
                                feed.decrementVoteCount();
                            }
                        }
                        
                        syncedCount++;
                    }
                    processedCount++;
                }
            }
            
            log.info("전체 피드 투표 수 동기화 완료 - 처리: {}개, 동기화: {}개", processedCount, syncedCount);
        } catch (Exception e) {
            log.error("전체 투표 수 동기화 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
