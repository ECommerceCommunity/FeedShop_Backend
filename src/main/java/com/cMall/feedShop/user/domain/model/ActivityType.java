package com.cMall.feedShop.user.domain.model;

public enum ActivityType {
    // 피드 관련 (백로그 요구사항에 맞게 수정 및 추가)
    FEED_CREATION(5, "피드 작성"),                    // 뱃지 점수: 5점 (기존 10점에서 수정)
    FEED_LIKE_RECEIVED(1, "피드 좋아요 받기"),        // 기존 유지
    FEED_LIKES_MILESTONE(2, "좋아요 마일스톤"),        // 신규 추가: 좋아요 100개당 2점
    EVENT_FEED_PARTICIPATION(2, "이벤트 피드 참여"),   // 신규 추가: 이벤트 피드 작성 2점
    COMMENT_DAILY_ACHIEVEMENT(1, "댓글 일일 달성"),   // 신규 추가: 하루 댓글 10개 작성 1점
    DIVERSE_PRODUCT_FEED(0, "다양 상품 피드"),         // 신규 추가: 다양 상품 피드 작성 0점 (포인트만)
    
    // 투표 관련
    VOTE_PARTICIPATION(1, "투표 참여"),
    
    // 댓글 관련
    COMMENT_CREATION(3, "댓글 작성"),
    COMMENT_LIKE_RECEIVED(1, "댓글 좋아요 받기"),
    
    // 리뷰 관련 (기존 시스템과 연계)
    REVIEW_CREATION(10, "리뷰 작성"),
    REVIEW_LIKE_RECEIVED(1, "리뷰 좋아요 받기"),
    
    // 구매 관련
    PURCHASE_COMPLETION(5, "구매 완료"),
    
    // 이벤트 관련
    EVENT_PARTICIPATION(2, "이벤트 참여"),
    EVENT_WINNER(50, "이벤트 수상"),
    
    // SNS 연계
    SNS_SHARING(3, "SNS 공유"),
    SNS_VERIFICATION(20, "SNS 계정 인증"),
    
    // 추천/초대 관련
    USER_REFERRAL(15, "사용자 추천"),
    REFERRAL_PURCHASE(10, "추천 사용자 구매");
    
    private final int points;
    private final String description;
    
    ActivityType(int points, String description) {
        this.points = points;
        this.description = description;
    }
    
    public int getPoints() {
        return points;
    }
    
    public String getDescription() {
        return description;
    }
}
