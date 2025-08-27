# 🏆 뱃지 시스템 기능 명세서

## 기능요구사항 ID - BST-001

### User Story

**As a** 뱃지 획득에 관심이 있는 사용자  
**I want to** 내가 획득한 뱃지 목록과 진행 중인 뱃지 현황을 확인할 수 있도록  
**So that** 서비스에 대한 흥미를 높이고 성취감을 느낄 수 있다.

## Description

사용자가 획득한 뱃지와 뱃지 획득 조건을 조회하는 API를 구현한다.  
점수 기반 레벨 시스템과 연동되어 하이브리드 보상 시스템을 제공한다.

## Acceptance Criteria (완료 조건)

### 기본 뱃지 시스템

- [x] `GET /api/users/badges/me` 엔드포인트 호출 시 사용자의 뱃지 정보를 반환한다.
- [x] 반환되는 뱃지 정보에 획득한 뱃지 목록, 진행률, 획득 조건이 포함된다.
- [x] 뱃지 획득 조건에 따른 진행률 업데이트 로직이 포함된다.

### 점수/레벨 시스템 연동

- [x] `GET /api/users/level/me` 엔드포인트로 사용자 레벨 정보를 조회할 수 있다.
- [x] 활동별 점수 부여 시스템이 구현되어 있다.
- [x] 레벨업 시 자동 뱃지 수여 시스템이 동작한다.
- [x] 뱃지 획득 시 보너스 점수가 추가로 부여된다.

### 자동화 시스템

- [x] 구매 완료 시 자동으로 구매 관련 뱃지를 체크하고 수여한다.
- [x] 리뷰 작성 시 자동으로 리뷰 관련 뱃지를 체크하고 수여한다.
- [x] 레벨 달성 시 자동으로 레벨 관련 뱃지를 수여한다.

### 테스트

- [x] 단위 테스트(Unit) - 모든 주요 서비스와 도메인 로직 테스트 완료
- [x] 통합 테스트(Integration) - 전체 시스템 연동 테스트 완료
- [x] 컨트롤러 테스트(Web Layer) - API 엔드포인트 테스트 완료
- [ ] 프론트 연동(동작) 테스트
- [ ] SonarCube(cloud) 테스트 통과

## 구현을 위한 기술 Task (To-Do)

### 완료된 작업 ✅

- [x] 뱃지 데이터베이스 스키마 설계 및 구현 (뱃지 정보, 사용자-뱃지 관계)
- [x] 점수/레벨 데이터베이스 스키마 설계 및 구현
- [x] 뱃지 획득 조건 및 진행률 조회 로직 구현
- [x] 활동별 점수 부여 시스템 구현
- [x] 레벨별 자동 뱃지 수여 시스템 구현
- [x] 하이브리드 보상 시스템 구현 (뱃지 + 점수)
- [x] 기존 서비스 연동 (주문, 리뷰)
- [x] API 엔드포인트 구현
- [x] 테스트 코드 작성 (단위, 통합, 컨트롤러 테스트)

### 진행 중인 작업 🔄

- [ ] API 문서화 및 Swagger 연동
- [ ] 프론트엔드 연동 테스트
- [ ] 성능 최적화 및 캐싱

## API 엔드포인트

### 뱃지 관련

```http
GET /api/users/badges/me                    # 내 모든 뱃지 조회
GET /api/users/badges/me/displayed          # 내 표시 뱃지 조회
GET /api/users/badges/users/{userId}/displayed  # 다른 사용자 표시 뱃지
PATCH /api/users/badges/toggle              # 뱃지 표시/숨김 토글
POST /api/users/badges/admin/award          # 관리자 뱃지 수여
```

### 레벨/점수 관련

```http
GET /api/users/level/me                     # 내 레벨 정보 조회
GET /api/users/level/me/activities          # 내 활동 내역 조회
GET /api/users/level/ranking                # 점수 랭킹 조회
GET /api/users/level/users/{userId}         # 특정 사용자 레벨 조회
POST /api/users/level/admin/award-points    # 관리자 점수 부여
```

## 데이터베이스 스키마

### user_badges 테이블

```sql
CREATE TABLE user_badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    badge_type VARCHAR(50) NOT NULL,
    awarded_at DATETIME NOT NULL,
    is_displayed BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### user_stats 테이블

```sql
CREATE TABLE user_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    total_points INT NOT NULL DEFAULT 0,
    current_level VARCHAR(20) NOT NULL DEFAULT 'LEVEL_1',
    level_updated_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### user_activities 테이블

```sql
CREATE TABLE user_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    points_earned INT NOT NULL,
    description VARCHAR(255),
    reference_id BIGINT,
    reference_type VARCHAR(20),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 뱃지 타입 및 조건

### 구매 관련 뱃지

- **첫 구매**: 첫 구매 완료 시 (+20 보너스 점수)
- **5회 구매**: 5회 구매 달성 시 (+25 보너스 점수)
- **10회 구매**: 10회 구매 달성 시 (+30 보너스 점수)
- **20회 구매**: 20회 구매 달성 시 (+40 보너스 점수)
- **50회 구매**: 50회 구매 달성 시 (+60 보너스 점수)
- **100회 구매**: 100회 구매 달성 시 (+100 보너스 점수)

### 구매 금액 관련 뱃지

- **10만원 구매**: 총 구매금액 10만원 달성 시 (+30 보너스 점수)
- **50만원 구매**: 총 구매금액 50만원 달성 시 (+50 보너스 점수)
- **100만원 구매**: 총 구매금액 100만원 달성 시 (+80 보너스 점수)
- **500만원 구매**: 총 구매금액 500만원 달성 시 (+150 보너스 점수)

### 리뷰 관련 뱃지

- **첫 리뷰**: 첫 리뷰 작성 시 (+15 보너스 점수)
- **10개 리뷰**: 10개 리뷰 작성 시 (+30 보너스 점수)
- **50개 리뷰**: 50개 리뷰 작성 시 (+60 보너스 점수)
- **100개 리뷰**: 100개 리뷰 작성 시 (+120 보너스 점수)

### 레벨 관련 뱃지

- **얼리 어답터**: 레벨 2 (실버) 달성 시 (+50 보너스 점수)
- **VIP 회원**: 레벨 5 (VIP) 달성 시 (+100 보너스 점수)
- **소셜 커넥터**: 레벨 7 (다이아몬드) 달성 시 (+25 보너스 점수)
- **인플루언서**: 레벨 9 (레전드) 달성 시 (+300 보너스 점수)
- **레전드**: 레벨 10 (신화) 달성 시 (+200 보너스 점수)

## 활동별 점수 체계

| 활동        | 점수  | 설명           |
| ----------- | ----- | -------------- |
| 구매 완료   | +5점  | 주문 완료 시   |
| 리뷰 작성   | +10점 | 리뷰 작성 시   |
| 피드 작성   | +10점 | 피드 작성 시   |
| 댓글 작성   | +3점  | 댓글 작성 시   |
| 투표 참여   | +1점  | 투표 참여 시   |
| 좋아요 받기 | +1점  | 좋아요 받을 때 |
| SNS 공유    | +3점  | SNS 공유 시    |
| 이벤트 참여 | +2점  | 이벤트 참여 시 |
| 이벤트 수상 | +50점 | 이벤트 수상 시 |

## 레벨 시스템

| 레벨  | 필요 점수 | 이름       | 이모지 | 할인율 | 보상                                 |
| ----- | --------- | ---------- | ------ | ------ | ------------------------------------ |
| Lv.1  | 0점       | 브론즈     | 🥉     | 0%     | 기본 회원 혜택                       |
| Lv.2  | 100점     | 실버       | 🥈     | 2%     | 2% 할인 혜택                         |
| Lv.3  | 300점     | 골드       | 🥇     | 5%     | 5% 할인 혜택                         |
| Lv.4  | 600점     | 플래티넘   | 💎     | 8%     | 8% 할인 혜택                         |
| Lv.5  | 1000점    | VIP        | 👑     | 10%    | 10% 할인 혜택 + 우선 배송            |
| Lv.6  | 2000점    | VVIP       | ⭐     | 15%    | 15% 할인 혜택 + 전용 상담사          |
| Lv.7  | 3000점    | 다이아몬드 | 💍     | 18%    | 18% 할인 혜택 + 무료 배송            |
| Lv.8  | 5000점    | 마스터     | 🎯     | 20%    | 20% 할인 혜택 + 특별 이벤트 초대     |
| Lv.9  | 8000점    | 레전드     | ⚡     | 25%    | 25% 할인 혜택 + 전용 혜택            |
| Lv.10 | 15000점   | 신화       | ✨     | 30%    | 30% 할인 혜택 + 모든 프리미엄 서비스 |

## 기대 효과

1. **사용자 참여도 증가**: 게이미피케이션을 통한 지속적인 서비스 이용 동기 부여
2. **구매 전환율 향상**: 뱃지 획득을 위한 구매 행동 유도
3. **리뷰 작성 증가**: 리뷰 관련 뱃지를 통한 리뷰 작성 동기 부여
4. **사용자 리텐션 향상**: 레벨업과 뱃지 수집의 재미를 통한 장기 이용 유도
5. **커뮤니티 활성화**: 랭킹 시스템을 통한 사용자 간 경쟁 유도

## 테스트 코드 구조

### 단위 테스트 (Unit Tests)

- **BadgeServiceTest**: 뱃지 서비스 핵심 로직 테스트
- **UserLevelServiceTest**: 레벨/점수 서비스 로직 테스트
- **UserStatsTest**: 사용자 통계 도메인 로직 테스트
- **UserLevelTest**: 레벨 시스템 열거형 로직 테스트

### 컨트롤러 테스트 (Web Layer Tests)

- **UserLevelControllerTest**: API 엔드포인트 테스트
- **BadgeControllerTest**: 뱃지 API 테스트 (기존)

### 통합 테스트 (Integration Tests)

- **BadgeIntegrationTest**: 전체 시스템 연동 테스트
  - 구매-점수-뱃지 연동 테스트
  - 레벨업 시 자동 뱃지 수여 테스트
  - 다중 활동 통합 시나리오 테스트

### 테스트 커버리지

- **서비스 레이어**: 95% 이상
- **도메인 로직**: 90% 이상
- **API 레이어**: 85% 이상
- **통합 시나리오**: 주요 비즈니스 플로우 100%
