package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.feed.domain.repository.FeedRewardEventRepository;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.RewardPolicyRepository;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 피드 리워드 이벤트 핸들러
 * 피드 관련 액션 발생 시 리워드 이벤트를 생성하고 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedRewardEventHandler {

    private final FeedRewardEventRepository feedRewardEventRepository;
    private final RewardPolicyRepository rewardPolicyRepository;
    private final ObjectMapper objectMapper;

    /**
     * 피드 생성 리워드 이벤트 생성
     */
    public void createFeedCreationEvent(User user, Feed feed) {
        try {
            // 피드 생성 리워드 정책 조회
            Optional<RewardPolicy> policyOpt = rewardPolicyRepository.findByRewardType(RewardType.FEED_CREATION);
            if (policyOpt.isEmpty()) {
                log.warn("피드 생성 리워드 정책을 찾을 수 없습니다");
                return;
            }

            RewardPolicy policy = policyOpt.get();
            
            // 일일 제한 확인
            if (!canReceiveDailyReward(user, policy)) {
                log.info("일일 제한에 도달했습니다 - userId: {}, rewardType: {}", user.getId(), policy.getRewardType());
                return;
            }

            // 중복 이벤트 방지
            if (feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(user, feed, policy.getRewardType())) {
                log.debug("이미 처리된 피드 생성 리워드 이벤트가 존재합니다 - userId: {}, feedId: {}", user.getId(), feed.getId());
                return;
            }

            // 관련 데이터 생성
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("feedType", feed.getFeedType());
            relatedData.put("hasEvent", feed.getEvent() != null);
            relatedData.put("eventType", "FEED_CREATION");

            String relatedDataJson = objectMapper.writeValueAsString(relatedData);

            // 리워드 이벤트 생성
            FeedRewardEvent event = FeedRewardEvent.builder()
                    .feed(feed)
                    .user(user)
                    .rewardType(policy.getRewardType())
                    .points(policy.getPoints())
                    .description("피드 생성 보상")
                    .relatedData(relatedDataJson)
                    .build();

            feedRewardEventRepository.save(event);
            log.info("피드 생성 리워드 이벤트 생성 완료 - userId: {}, feedId: {}, points: {}", 
                    user.getId(), feed.getId(), policy.getPoints());

        } catch (Exception e) {
            log.error("피드 생성 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}", user.getId(), feed.getId(), e);
        }
    }

    /**
     * 피드 좋아요 마일스톤 리워드 이벤트 생성
     */
    public void createFeedLikesMilestoneEvent(User user, Feed feed, int currentLikeCount) {
        try {
            // 좋아요 마일스톤 리워드 정책 조회
            Optional<RewardPolicy> policyOpt = rewardPolicyRepository.findByRewardType(RewardType.FEED_LIKES_MILESTONE);
            if (policyOpt.isEmpty()) {
                log.warn("피드 좋아요 마일스톤 리워드 정책을 찾을 수 없습니다");
                return;
            }

            RewardPolicy policy = policyOpt.get();
            
            // 마일스톤 달성 확인 (예: 10, 50, 100, 500, 1000)
            if (!isLikesMilestone(currentLikeCount)) {
                log.debug("좋아요 마일스톤을 달성하지 못했습니다 - likeCount: {}", currentLikeCount);
                return;
            }

            // 일일 제한 확인
            if (!canReceiveDailyReward(user, policy)) {
                log.info("일일 제한에 도달했습니다 - userId: {}, rewardType: {}", user.getId(), policy.getRewardType());
                return;
            }

            // 중복 이벤트 방지
            if (feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(user, feed, policy.getRewardType())) {
                log.debug("이미 처리된 좋아요 마일스톤 리워드 이벤트가 존재합니다 - userId: {}, feedId: {}", user.getId(), feed.getId());
                return;
            }

            // 관련 데이터 생성
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("likeCount", currentLikeCount);
            relatedData.put("milestone", currentLikeCount);
            relatedData.put("eventType", "FEED_LIKES_MILESTONE");

            String relatedDataJson = objectMapper.writeValueAsString(relatedData);

            // 리워드 이벤트 생성
            FeedRewardEvent event = FeedRewardEvent.builder()
                    .feed(feed)
                    .user(user)
                    .rewardType(policy.getRewardType())
                    .points(policy.getPoints())
                    .description("피드 좋아요 마일스톤 달성 보상")
                    .relatedData(relatedDataJson)
                    .build();

            feedRewardEventRepository.save(event);
            log.info("피드 좋아요 마일스톤 리워드 이벤트 생성 완료 - userId: {}, feedId: {}, likeCount: {}, points: {}", 
                    user.getId(), feed.getId(), currentLikeCount, policy.getPoints());

        } catch (Exception e) {
            log.error("피드 좋아요 마일스톤 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}", user.getId(), feed.getId(), e);
        }
    }

    /**
     * 이벤트 피드 참여 리워드 이벤트 생성
     */
    public void createEventFeedParticipationEvent(User user, Feed feed, Long eventId) {
        try {
            // 이벤트 피드 참여 리워드 정책 조회
            Optional<RewardPolicy> policyOpt = rewardPolicyRepository.findByRewardType(RewardType.EVENT_FEED_PARTICIPATION);
            if (policyOpt.isEmpty()) {
                log.warn("이벤트 피드 참여 리워드 정책을 찾을 수 없습니다");
                return;
            }

            RewardPolicy policy = policyOpt.get();
            
            // 일일 제한 확인
            if (!canReceiveDailyReward(user, policy)) {
                log.info("일일 제한에 도달했습니다 - userId: {}, rewardType: {}", user.getId(), policy.getRewardType());
                return;
            }

            // 중복 이벤트 방지
            if (feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(user, feed, policy.getRewardType())) {
                log.debug("이미 처리된 이벤트 피드 참여 리워드 이벤트가 존재합니다 - userId: {}, feedId: {}", user.getId(), feed.getId());
                return;
            }

            // 관련 데이터 생성
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("eventId", eventId);
            relatedData.put("feedType", feed.getFeedType());
            relatedData.put("eventType", "EVENT_FEED_PARTICIPATION");

            String relatedDataJson = objectMapper.writeValueAsString(relatedData);

            // 리워드 이벤트 생성
            FeedRewardEvent event = FeedRewardEvent.builder()
                    .feed(feed)
                    .user(user)
                    .rewardType(policy.getRewardType())
                    .points(policy.getPoints())
                    .description("이벤트 피드 참여 보상")
                    .relatedData(relatedDataJson)
                    .build();

            feedRewardEventRepository.save(event);
            log.info("이벤트 피드 참여 리워드 이벤트 생성 완료 - userId: {}, feedId: {}, eventId: {}, points: {}", 
                    user.getId(), feed.getId(), eventId, policy.getPoints());

        } catch (Exception e) {
            log.error("이벤트 피드 참여 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}, eventId: {}", 
                    user.getId(), feed.getId(), eventId, e);
        }
    }

    /**
     * 댓글 일일 달성 리워드 이벤트 생성
     */
    public void createCommentDailyAchievementEvent(User user, Feed feed) {
        try {
            // 댓글 일일 달성 리워드 정책 조회
            Optional<RewardPolicy> policyOpt = rewardPolicyRepository.findByRewardType(RewardType.COMMENT_DAILY_ACHIEVEMENT);
            if (policyOpt.isEmpty()) {
                log.warn("댓글 일일 달성 리워드 정책을 찾을 수 없습니다");
                return;
            }

            RewardPolicy policy = policyOpt.get();
            
            // 일일 제한 확인
            if (!canReceiveDailyReward(user, policy)) {
                log.info("일일 제한에 도달했습니다 - userId: {}, rewardType: {}", user.getId(), policy.getRewardType());
                return;
            }

            // 중복 이벤트 방지
            if (feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(user, feed, policy.getRewardType())) {
                log.debug("이미 처리된 댓글 일일 달성 리워드 이벤트가 존재합니다 - userId: {}, feedId: {}", user.getId(), feed.getId());
                return;
            }

            // 관련 데이터 생성
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("eventType", "COMMENT_DAILY_ACHIEVEMENT");
            relatedData.put("feedId", feed.getId());

            String relatedDataJson = objectMapper.writeValueAsString(relatedData);

            // 리워드 이벤트 생성
            FeedRewardEvent event = FeedRewardEvent.builder()
                    .feed(feed)
                    .user(user)
                    .rewardType(policy.getRewardType())
                    .points(policy.getPoints())
                    .description("댓글 일일 달성 보상")
                    .relatedData(relatedDataJson)
                    .build();

            feedRewardEventRepository.save(event);
            log.info("댓글 일일 달성 리워드 이벤트 생성 완료 - userId: {}, feedId: {}, points: {}", 
                    user.getId(), feed.getId(), policy.getPoints());

        } catch (Exception e) {
            log.error("댓글 일일 달성 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}", user.getId(), feed.getId(), e);
        }
    }

    /**
     * 다양한 상품 피드 리워드 이벤트 생성
     */
    public void createDiverseProductFeedEvent(User user, Feed feed, int productCount) {
        try {
            // 다양한 상품 피드 리워드 정책 조회
            Optional<RewardPolicy> policyOpt = rewardPolicyRepository.findByRewardType(RewardType.DIVERSE_PRODUCT_FEED);
            if (policyOpt.isEmpty()) {
                log.warn("다양한 상품 피드 리워드 정책을 찾을 수 없습니다");
                return;
            }

            RewardPolicy policy = policyOpt.get();
            
            // 상품 다양성 기준 확인 (예: 3개 이상의 서로 다른 상품)
            if (productCount < 3) {
                log.debug("상품 다양성 기준을 달성하지 못했습니다 - productCount: {}", productCount);
                return;
            }

            // 일일 제한 확인
            if (!canReceiveDailyReward(user, policy)) {
                log.info("일일 제한에 도달했습니다 - userId: {}, rewardType: {}", user.getId(), policy.getRewardType());
                return;
            }

            // 중복 이벤트 방지
            if (feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(user, feed, policy.getRewardType())) {
                log.debug("이미 처리된 다양한 상품 피드 리워드 이벤트가 존재합니다 - userId: {}, feedId: {}", user.getId(), feed.getId());
                return;
            }

            // 관련 데이터 생성
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("productCount", productCount);
            relatedData.put("eventType", "DIVERSE_PRODUCT_FEED");

            String relatedDataJson = objectMapper.writeValueAsString(relatedData);

            // 리워드 이벤트 생성
            FeedRewardEvent event = FeedRewardEvent.builder()
                    .feed(feed)
                    .user(user)
                    .rewardType(policy.getRewardType())
                    .points(policy.getPoints())
                    .description("다양한 상품 피드 작성 보상")
                    .relatedData(relatedDataJson)
                    .build();

            feedRewardEventRepository.save(event);
            log.info("다양한 상품 피드 리워드 이벤트 생성 완료 - userId: {}, feedId: {}, productCount: {}, points: {}", 
                    user.getId(), feed.getId(), productCount, policy.getPoints());

        } catch (Exception e) {
            log.error("다양한 상품 피드 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}, productCount: {}", 
                    user.getId(), feed.getId(), productCount, e);
        }
    }

    /**
     * 일일 리워드 수령 가능 여부 확인
     */
    private boolean canReceiveDailyReward(User user, RewardPolicy policy) {
        LocalDate today = LocalDate.now();
        
        // 오늘 받은 리워드 수 확인
        long todayRewardCount = feedRewardEventRepository.countDailyEventsByUserAndType(
                user, 
                policy.getRewardType(), 
                today.atStartOfDay()
        );
        
        return todayRewardCount < policy.getDailyLimit();
    }

    /**
     * 좋아요 마일스톤 달성 여부 확인
     */
    private boolean isLikesMilestone(int likeCount) {
        // 마일스톤 기준: 10, 50, 100, 500, 1000
        int[] milestones = {10, 50, 100, 500, 1000};
        for (int milestone : milestones) {
            if (likeCount == milestone) {
                return true;
            }
        }
        return false;
    }
}
