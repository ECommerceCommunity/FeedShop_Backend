package com.cMall.feedShop.user.infrastructure.repository;

import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RewardPolicyJpaRepository extends JpaRepository<RewardPolicy, Long> {
    
    // 활성화된 정책 조회
    List<RewardPolicy> findByIsActiveTrue();
    
    // 특정 타입의 활성화된 정책 조회
    Optional<RewardPolicy> findByRewardTypeAndIsActiveTrue(RewardType rewardType);
    
    // 특정 타입의 정책 조회 (활성화 여부 무관)
    Optional<RewardPolicy> findByRewardType(RewardType rewardType);
    
    // 유효한 정책들 조회 (활성화 + 기간 유효)
    @Query("SELECT rp FROM RewardPolicy rp WHERE rp.isActive = true " +
           "AND (rp.validFrom IS NULL OR rp.validFrom <= CURRENT_TIMESTAMP) " +
           "AND (rp.validTo IS NULL OR rp.validTo >= CURRENT_TIMESTAMP)")
    List<RewardPolicy> findValidPolicies();
    
    // 특정 타입의 유효한 정책 조회
    @Query("SELECT rp FROM RewardPolicy rp WHERE rp.rewardType = :rewardType " +
           "AND rp.isActive = true " +
           "AND (rp.validFrom IS NULL OR rp.validFrom <= CURRENT_TIMESTAMP) " +
           "AND (rp.validTo IS NULL OR rp.validTo >= CURRENT_TIMESTAMP)")
    Optional<RewardPolicy> findValidPolicyByType(@Param("rewardType") RewardType rewardType);
}
