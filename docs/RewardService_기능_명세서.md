# 🎁 RewardService 기능 명세서

## 📋 개요

RewardService는 FeedShop의 리워드 시스템을 관리하는 핵심 서비스입니다. 사용자의 다양한 활동에 대한 포인트 보상 지급, 리워드 정책 관리, 히스토리 추적을 담당합니다.

## 🏗️ 아키텍처

### 도메인 모델

- **RewardType**: 리워드 타입 enum (관리자 지급, 이벤트 참여, 리뷰 작성 등)
- **RewardPolicy**: 리워드 정책 정보 (포인트 금액, 제한 사항, 유효 기간)
- **RewardHistory**: 리워드 지급 히스토리 (지급 내역, 처리 상태)

### Repository

- **RewardPolicyRepository**: 리워드 정책 관리
- **RewardHistoryRepository**: 리워드 히스토리 관리

## 🔧 구현된 기능

### 1. 관리자 포인트 지급

```java
public RewardHistoryResponse grantPointsByAdmin(RewardGrantRequest request, UserDetails adminDetails)
```

**기능**: 관리자가 사용자에게 직접 포인트 지급

- **권한 검증**: ADMIN 권한만 가능
- **포인트 검증**: 1 이상의 양수만 지급 가능
- **히스토리 기록**: 지급 사유와 관리자 정보 기록
- **자동 처리**: PointService와 연동하여 즉시 포인트 적립

**요청 예시**:

```json
{
  "userId": 123,
  "points": 1000,
  "description": "고객 만족도 조사 참여 보상"
}
```

### 2. 리뷰 작성 보상

```java
public RewardHistoryResponse grantReviewReward(User user, Long reviewId, String reviewType)
```

**기능**: 리뷰 작성 시 자동 포인트 지급

- **리뷰 타입별 차등 보상**:
  - 일반 리뷰: 기본 포인트
  - 사진 리뷰: 추가 포인트
  - 고품질 리뷰: 추가 포인트
- **중복 지급 방지**: 동일 리뷰에 대한 중복 보상 차단
- **정책 기반 지급**: RewardPolicy에 정의된 포인트 금액 적용

### 3. 이벤트 참여 보상

```java
public RewardHistoryResponse grantEventReward(User user, Long eventId, RewardType eventRewardType)
```

**기능**: 이벤트 참여/당첨 시 포인트 지급

- **이벤트 타입별 보상**:
  - EVENT_PARTICIPATION: 참여 보상
  - EVENT_WINNER: 당첨 보상
- **제한 사항 확인**: 일일/월간 획득 제한 검증
- **중복 지급 방지**: 동일 이벤트에 대한 중복 보상 차단

### 4. 생일 축하 포인트

```java
public RewardHistoryResponse grantBirthdayReward(User user)
```

**기능**: 생일 당일 자동 포인트 지급

- **연도별 1회 지급**: 같은 연도에 중복 지급 방지
- **자동 처리**: 생일 당일 자동 실행 (스케줄러 연동 예정)

### 5. 첫 구매 보너스

```java
public RewardHistoryResponse grantFirstPurchaseReward(User user, Long orderId)
```

**기능**: 첫 주문 시 추가 포인트 지급

- **1회성 보상**: 계정당 1회만 지급
- **주문 연동**: OrderService에서 첫 주문 감지 시 호출

### 6. 리워드 히스토리 조회

```java
public Page<RewardHistoryResponse> getRewardHistory(UserDetails userDetails, int page, int size)
```

**기능**: 사용자의 리워드 지급 내역 조회

- **페이징 지원**: 대용량 데이터 처리
- **상세 정보**: 지급 사유, 처리 상태, 관련 엔티티 정보

### 7. 리워드 정책 조회

```java
public List<RewardPolicyResponse> getRewardPolicies()
```

**기능**: 현재 유효한 리워드 정책 목록 조회

- **활성 정책만**: isActive=true인 정책만 조회
- **기간 유효성**: validFrom/validTo 기간 내 정책만 조회

### 8. 미처리 리워드 처리

```java
public void processPendingRewards()
```

**기능**: 미처리 리워드 일괄 처리 (스케줄러용)

- **배치 처리**: 모든 미처리 리워드를 포인트로 적립
- **에러 처리**: 개별 실패 시에도 전체 프로세스 계속 진행
- **로그 기록**: 처리 결과 상세 로깅

## 📊 리워드 정책

### 정책 구성 요소

- **포인트 금액**: 지급할 포인트 수량
- **일일 제한**: 하루 최대 획득 횟수
- **월간 제한**: 한 달 최대 획득 횟수
- **유효 기간**: 정책 적용 기간 (validFrom ~ validTo)
- **활성 상태**: 정책 사용 여부

### 기본 정책 예시

```java
// 리뷰 작성 보상
RewardPolicy.builder()
    .rewardType(RewardType.REVIEW_WRITE)
    .points(100)
    .description("리뷰 작성 보상")
    .dailyLimit(5)
    .monthlyLimit(20)
    .build();

// 이벤트 참여 보상
RewardPolicy.builder()
    .rewardType(RewardType.EVENT_PARTICIPATION)
    .points(500)
    .description("이벤트 참여 보상")
    .dailyLimit(3)
    .monthlyLimit(10)
    .build();

// 생일 축하 포인트
RewardPolicy.builder()
    .rewardType(RewardType.BIRTHDAY)
    .points(1000)
    .description("생일 축하 포인트")
    .build();
```

## 🔗 연동 시스템

### 1. PointService 연동

- 모든 리워드는 PointService.earnPoints()를 통해 포인트 적립
- 리워드 히스토리와 포인트 거래 내역 모두 기록

### 2. ReviewService 연동

- 리뷰 작성 완료 시 자동으로 리워드 지급
- 리뷰 타입에 따른 차등 보상 적용

### 3. EventService 연동

- 이벤트 참여/당첨 시 자동으로 리워드 지급
- 이벤트별 차등 보상 정책 적용

### 4. OrderService 연동

- 첫 구매 감지 시 자동으로 보너스 지급
- 주문 완료 시 기존 포인트 적립과 별도 처리

## 📝 API 엔드포인트

### 관리자 API

- `POST /api/rewards/admin/grant` - 관리자 포인트 지급

### 사용자 API

- `GET /api/rewards/history` - 리워드 히스토리 조회
- `GET /api/rewards/policies` - 리워드 정책 조회

## 🔒 보안 및 검증

### 권한 검증

- 관리자 포인트 지급: ADMIN 권한 필수
- 사용자 데이터 조회: 본인 데이터만 조회 가능

### 입력 검증

- 포인트 금액: 1 이상의 양수만 허용
- 사용자 ID: 유효한 사용자만 대상으로 지급
- 지급 사유: 필수 입력 항목

### 중복 지급 방지

- 리뷰/이벤트: 동일 엔티티에 대한 중복 보상 차단
- 생일/첫구매: 계정당 1회만 지급

## 📈 성능 고려사항

### 데이터베이스 최적화

- 리워드 히스토리 인덱싱 (사용자, 타입, 날짜)
- 페이징 처리로 대용량 데이터 처리
- 미처리 리워드 조회 최적화

### 배치 처리

- 미처리 리워드 일괄 처리
- 에러 발생 시에도 전체 프로세스 계속 진행

## 🚧 향후 확장 계획

### 1. 스케줄러 연동

- 생일 축하 포인트 자동 지급
- 만료 예정 리워드 알림
- 정기 리워드 지급

### 2. 추천인 시스템

- 친구 추천 시 양방향 보상
- 추천인 코드 관리
- 추천인별 차등 보상

### 3. 리워드 통계

- 사용자별 리워드 획득 통계
- 리워드 타입별 지급 통계
- 관리자 대시보드

### 4. 리워드 이벤트

- 시즌별 특별 리워드
- 연속 로그인 보상
- 구매 금액별 추가 보상

## 🧪 테스트 계획

### 단위 테스트

- 각 리워드 지급 메서드별 테스트
- 정책 검증 로직 테스트
- 제한 사항 검증 테스트

### 통합 테스트

- PointService 연동 테스트
- 다른 서비스와의 연동 테스트
- 전체 리워드 플로우 테스트
