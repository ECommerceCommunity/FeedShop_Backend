# 🎯 PointService 기능 명세서

## 📋 개요

PointService는 FeedShop의 포인트 시스템을 관리하는 핵심 서비스입니다. 사용자의 포인트 적립, 사용, 조회 및 만료 관리를 담당합니다.

## 🏗️ 아키텍처

### 도메인 모델

- **UserPoint**: 사용자별 포인트 잔액 정보
- **PointTransaction**: 포인트 거래 내역 (적립, 사용, 취소, 만료)
- **PointTransactionType**: 거래 타입 (EARN, USE, CANCEL, EXPIRE)

### Repository

- **UserPointRepository**: 사용자 포인트 정보 관리
- **PointTransactionRepository**: 포인트 거래 내역 관리

## 🔧 구현된 기능

### 1. 포인트 잔액 조회

```java
public PointBalanceResponse getPointBalance(UserDetails userDetails)
```

**기능**: 현재 사용자의 포인트 잔액 및 통계 정보 조회

- 현재 보유 포인트
- 총 적립 포인트 (만료되지 않은)
- 총 사용 포인트
- 총 만료 포인트

**응답 예시**:

```json
{
  "currentPoints": 1500,
  "totalEarnedPoints": 2000,
  "totalUsedPoints": 500,
  "totalExpiredPoints": 0
}
```

### 2. 포인트 거래 내역 조회

#### 2.1 전체 거래 내역 조회 (페이징)

```java
public PointTransactionPageResponse getPointTransactions(UserDetails userDetails, int page, int size)
```

#### 2.2 타입별 거래 내역 조회

```java
public PointTransactionPageResponse getPointTransactionsByType(UserDetails userDetails, PointTransactionType type, int page, int size)
```

#### 2.3 기간별 거래 내역 조회

```java
public PointTransactionPageResponse getPointTransactionsByPeriod(UserDetails userDetails, LocalDateTime startDate, LocalDateTime endDate, int page, int size)
```

#### 2.4 주문별 거래 내역 조회

```java
public List<PointTransactionResponse> getPointTransactionsByOrder(UserDetails userDetails, Long orderId)
```

### 3. 포인트 적립

```java
public PointTransactionResponse earnPoints(User user, Integer points, String description, Long orderId)
```

**기능**: 사용자에게 포인트 적립

- **적립 조건**: 0보다 큰 양수
- **만료일**: 적립일로부터 1년 후
- **거래 내역**: EARN 타입으로 기록

**사용 예시**:

```java
// 주문 완료 시 포인트 적립
pointService.earnPoints(user, 500, "주문 적립", orderId);
```

### 4. 포인트 사용

```java
public PointTransactionResponse usePoints(User user, Integer points, String description, Long orderId)
```

**기능**: 사용자의 포인트 사용

- **사용 조건**: 보유 포인트 이상 사용 불가
- **거래 내역**: USE 타입으로 기록

**사용 예시**:

```java
// 주문 시 포인트 사용
pointService.usePoints(user, 1000, "주문 결제", orderId);
```

### 5. 포인트 취소

```java
public PointTransactionResponse cancelPoints(User user, Integer points, String description, Long orderId)
```

**기능**: 사용된 포인트 취소 (주문 취소 시)

- **취소 처리**: 사용된 포인트를 다시 적립
- **거래 내역**: CANCEL 타입으로 기록

### 6. 만료 예정 포인트 조회

```java
public ExpiringPointResponse getExpiringPoints(UserDetails userDetails)
```

**기능**: 30일 이내 만료 예정인 포인트 조회

- 만료 예정 포인트 목록
- 총 만료 예정 포인트 금액

### 7. 만료된 포인트 처리

```java
public void processExpiredPoints()
```

**기능**: 만료된 포인트 자동 처리 (스케줄러용)

- 모든 사용자의 만료된 포인트 조회
- 만료 거래 내역 생성 (EXPIRE 타입)

## 🔗 OrderCommonService 연동

### 주문 시 포인트 처리

OrderCommonService에서 주문 완료 시 자동으로 포인트 처리:

```java
// 포인트 사용
if (usedPoints != null && usedPoints > 0) {
    pointService.usePoints(user, usedPoints, "주문 결제", orderId);
}

// 포인트 적립
if (earnedPoints != null && earnedPoints > 0) {
    pointService.earnPoints(user, earnedPoints, "주문 적립", orderId);
}
```

## 📊 포인트 정책

### 적립 정책

- **주문 적립**: 주문 금액의 0.5% 적립
- **만료 기간**: 적립일로부터 1년
- **최소 적립 단위**: 1포인트

### 사용 정책

- **사용 단위**: 100포인트 단위로만 사용 가능
- **최대 사용**: 보유 포인트 범위 내에서만 사용
- **사용 제한**: 주문 시에만 사용 가능

### 취소 정책

- **취소 가능**: 주문 취소 시에만 포인트 취소 가능
- **취소 처리**: 사용된 포인트를 원래 만료일로 복원

## 🧪 테스트 커버리지

### 테스트 케이스 (16개)

1. ✅ 포인트 잔액 조회 성공
2. ✅ 포인트 잔액 조회 실패 - 사용자 없음
3. ✅ 포인트 거래 내역 조회 성공
4. ✅ 특정 타입 포인트 거래 내역 조회 성공
5. ✅ 만료 예정 포인트 조회 성공
6. ✅ 주문별 포인트 거래 내역 조회 성공
7. ✅ 포인트 적립 성공
8. ✅ 포인트 적립 실패 - 유효하지 않은 포인트 금액
9. ✅ 포인트 사용 성공
10. ✅ 포인트 사용 실패 - 포인트 부족
11. ✅ 포인트 사용 실패 - 유효하지 않은 포인트 금액
12. ✅ 포인트 취소 성공
13. ✅ 포인트 취소 실패 - 유효하지 않은 포인트 금액
14. ✅ 사용자 포인트 정보 없을 때 새로 생성
15. ✅ 기간별 포인트 거래 내역 조회 성공
16. ✅ 포인트 거래 내역 조회 성공

## 🚧 미구현 기능

### 향후 구현 예정 기능

1. **관리자 포인트 발급**

   - 관리자가 사용자에게 직접 포인트 지급
   - 지급 사유 및 관리자 정보 기록

2. **이벤트 참여 보상**

   - 이벤트 참여 시 포인트 지급
   - 이벤트별 차등 보상

3. **리뷰 작성 보상**

   - 리뷰 작성 시 포인트 지급
   - 리뷰 품질에 따른 차등 보상

4. **추천인 보상**

   - 친구 추천 시 포인트 지급
   - 추천인/피추천인 모두 보상

5. **생일 축하 포인트**

   - 생일 당일 자동 포인트 지급

6. **첫 구매 보너스**
   - 첫 주문 시 추가 포인트 지급

## 📝 API 엔드포인트

### 사용자 포인트 API

- `GET /api/users/points/balance` - 포인트 잔액 조회
- `GET /api/users/points/transactions` - 거래 내역 조회
- `GET /api/users/points/transactions/type/{type}` - 타입별 거래 내역
- `GET /api/users/points/transactions/period` - 기간별 거래 내역
- `GET /api/users/points/transactions/order/{orderId}` - 주문별 거래 내역
- `GET /api/users/points/expiring` - 만료 예정 포인트 조회

## 🔒 보안 및 검증

### 입력 검증

- 포인트 금액: null 또는 0 이하 금액 거부
- 사용자 검증: 유효한 사용자만 포인트 조회/사용 가능
- 포인트 사용: 보유 포인트 범위 내에서만 사용 가능

### 로깅

- 모든 포인트 거래에 대한 상세 로그 기록
- 사용자 ID, 포인트 금액, 거래 타입, 설명 정보 포함

## 📈 성능 고려사항

### 데이터베이스 최적화

- 포인트 거래 내역 인덱싱
- 페이징 처리로 대용량 데이터 처리
- 만료 포인트 조회 최적화

### 캐싱 전략

- 사용자 포인트 잔액 캐싱 (Redis 활용 예정)
- 자주 조회되는 통계 정보 캐싱

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025-08-12  
**작성자**: 개발팀
