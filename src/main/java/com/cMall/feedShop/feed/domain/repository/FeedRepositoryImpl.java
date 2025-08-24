package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.entity.Feed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepository {
    
    private final FeedJpaRepository feedJpaRepository;
    
    @Override
    public Feed save(Feed feed) {
        return feedJpaRepository.save(feed);
    }
    
    @Override
    public Optional<Feed> findById(Long id) {
        return feedJpaRepository.findById(id);
    }
    
    @Override
    public void delete(Feed feed) {
        feedJpaRepository.delete(feed);
    }
    
    @Override
    public boolean existsByOrderItemIdAndUserId(Long orderItemId, Long userId) {
        return feedJpaRepository.existsByOrderItemOrderItemIdAndUserId(orderItemId, userId);
    }
    
    @Override
    public Page<Feed> findAll(Pageable pageable) {
        return feedJpaRepository.findAllActive(pageable);
    }
    
    @Override
    public Page<Feed> findByUserId(Long userId, Pageable pageable) {
        return feedJpaRepository.findByUserId(userId, pageable);
    }
    
    @Override
    public Page<Feed> findByFeedType(String feedType, Pageable pageable) {
        return feedJpaRepository.findByFeedType(com.cMall.feedShop.feed.domain.enums.FeedType.valueOf(feedType), pageable);
    }
    
    @Override
    public List<Feed> findByEventId(Long eventId) {
        return feedJpaRepository.findByEventId(eventId);
    }
    
    @Override
    public List<Feed> findByOrderItemId(Long orderItemId) {
        return feedJpaRepository.findByOrderItemOrderItemId(orderItemId);
    }

    @Override
    public Optional<Feed> findDetailById(Long id) {
        return feedJpaRepository.findDetailById(id);
    }

    @Override
    public Optional<Feed> findDetailWithAllById(Long id) {
        return feedJpaRepository.findDetailWithAllById(id);
    }
    
    @Override
    public Page<Feed> findByUserIdAndFeedType(Long userId, String feedType, Pageable pageable) {
        return feedJpaRepository.findByUserIdAndFeedTypeActive(
                userId, 
                com.cMall.feedShop.feed.domain.enums.FeedType.valueOf(feedType), 
                pageable
        );
    }
    
    @Override
    public long countByUserId(Long userId) {
        return feedJpaRepository.countByUserIdActive(userId);
    }
    
    @Override
    public long countByUserIdAndFeedType(Long userId, String feedType) {
        return feedJpaRepository.countByUserIdAndFeedTypeActive(
                userId, 
                com.cMall.feedShop.feed.domain.enums.FeedType.valueOf(feedType)
        );
    }
} 