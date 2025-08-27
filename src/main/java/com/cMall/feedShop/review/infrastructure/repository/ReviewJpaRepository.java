package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    // 기본 CRUD 메서드만 제공
    // 복잡한 쿼리는 QueryDSL로 처리

    /**
     * 리뷰 조회 시 User와 UserProfile을 함께 fetch join으로 조회
     */
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH u.userProfile up " +
           "WHERE r.reviewId = :reviewId")
    Optional<Review> findByIdWithUserProfile(@Param("reviewId") Long reviewId);

}