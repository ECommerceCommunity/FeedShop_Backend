# 이벤트·피드 성능 개선 로드맵

문서(Problem → Thinking → Solution → Result) 흐름에 맞춘 **실행 단위**입니다.  
**커밋·푸시는 각 단위 완료 후 승인 시에만** 진행합니다.

**트러블슈팅:** 작업 전부 끝난 뒤 **별도 문서**로 정리합니다. 이 파일에는 로드맵만 둡니다.

---

## 포트폴리오·시각 자료 (이미지 보강 가이드)

포폴에는 **문장만 있는 Result**보다 **개선 전·개선 후가 한눈에 보이는 이미지**가 설득력을 크게 높입니다. 기술 Phase와 **같은 순서로** 아래를 진행하면, 과정·결과를 캡처해 빈칸을 채우기 쉽습니다.

### 공통 원칙

- **Before / After 쌍:** 같은 API, 같은 조건(페이지 크기, 정렬, 데이터 대략 규모, 사용 도구)으로 촬영할 것.
- **한 이미지 한 메시지:** 제목·범례·(선택) 브랜치명·날짜를 작게 넣어 나중에 설명할 때 헷갈리지 않게.
- **출처 표기:** Swagger·Postman·브라우저 Network·터미널·Redis CLI·k6 리포트 등 **어떤 화면인지** 슬라이드 캡션 또는 이미지 안 작은 텍스트로 표기.

### 단계별로 넣으면 좋은 것 (체크리스트)

| 순서 | 기술·문서 단계 | 포트폴리오에 넣기 좋은 캡처·산출물 |
|------|----------------|-----------------------------------|
| 0 | **브랜치·범위 고정** | GitHub 브랜치 화면 또는 `git log -1 --oneline` — “이 브랜치에서 다룬 범위” 한 장 |
| 1 | **1-1 기준선(Before)** | 대상 API 호출(Swagger/Postman) + **응답 시간**(Network **Timing** 또는 클라이언트 표시) + 가능하면 **SQL/Hibernate 로그** 또는 **쿼리 개수** + k6 **`summary` 터미널** 또는 **HTML 리포트 상단 요약** |
| 2 | **1-2 `searchEvents` 개선 후(After)** | 동일 시나리오 재측정 — **latency·쿼리 수** Before·After 비교(두 컷 나란히 또는 표 한 장) |
| 3 | **1-3 `getAllEvents` 개선 후** | 목록 API 동일 방식 Before/After(개선 전 Before는 1-1에서 미리 확보하는 것을 권장) |
| 4 | **1-4 Phase 1 정리** | 표 한 장: API명 · Before(p95 등) · After · 적용 기법 한 줄 |
| 5 | **2A-1 Redis·Compose** | `docker compose ps` 또는 Docker Desktop에서 **redis/mysql** 기동 화면, `application-redis` **비밀번호 제외** 일부 |
| 6 | **2A-2~3 캐시** | `redis` 프로파일에서 **키/TTL**(redis-cli 또는 GUI), 또는 로그에 **캐시 재조회 여부**가 드러나는 한 줄 |
| 7 | **2A-4 Evict** | 이벤트 생성/수정 후 동일 피드 API 재호출 시 **DB 재조회**가 보이는 로그 또는 응답 지연 패턴(선택) |
| 8 | **2A-5 miss→hit** | **첫 요청 vs 두 번째 요청** 지연·쿼리 수 차이(캐시 효과 한 장으로 설명) |
| 9 | **2-B(도입 시)** | 투표 전후 Redis 수치·DB 유니크·동시 요청 결과(k6 등) |
| 10 | **Phase 3 Result** | Problem→Thinking→Solution→**Result** 슬라이드용 — **수치가 들어간 Result** 한 장 |
| 11 | **Phase 4 트러블슈팅** | 이슈 1건당: **증상 캡처 + 원인 한 줄 + 해결(커밋/PR 링크)** 미니 케이스 |

### 이미 개선 코드가 있는 경우(현 `feature/…` 브랜치)

- **After:** 위 표에서 해당 기능이 구현된 행부터 **지금 환경**에서 바로 캡처 가능.
- **Before:** (1) `develop` 등 개선 **이전 커밋**에 잠시 체크아웃해 **동일 도구·동일 시나리오**로 재측정하거나, (2) 당시 메모·PR에 남은 수치만 있으면 **표로 정리**하고, 나중에 동일 조건으로 스크린샷만 보강.

---

## Phase 1 — 쿼리 최적화 (문서 1단계)

**목표:** 연관 로딩 N+1·비효율 쿼리 완화, `findAll` + 메모리 필터 우려 정리.

| 단위 | 내용 | 종료 시 확인 |
|------|------|----------------|
| 1-1 | 기준선: 대상 API·p95·쿼리 수(가능 시) 기록 — Result/트러블슈팅용 Before | 수치·재현 조건 메모 |
| 1-2 | `searchEvents` / QueryDSL: 컬렉션 fetchJoin + 페이징 구조 보완(2-step 또는 DTO 프로젝션 등 택1) | 검색 경로 재측정 |
| 1-3 | `getAllEvents` / JPA: 연관 로딩 전략(fetch join 전용·`@EntityGraph`·batch size 등) 확정 및 적용 | 목록 경로 재측정 |
| 1-4 | Phase 1 종료: Before/After 1차 정리 | 문서 Result 초안 반영 가능 여부 판단 |

---

## Phase 2-A — 이벤트 읽기 캐시 + Redis (문서 2단계 · 이벤트 목록)

**목표:** 조회 많음·변경 적음 → 캐시 적합 구간에 Redis(`RedisCacheManager`), 분산 환경 대응.

| 단위 | 내용 | 종료 시 확인 |
|------|------|----------------|
| 2A-1 | R1: `spring-data-redis`, 로컬 Redis(docker-compose 등), `spring.data.redis.*`, 연결 스모크 | **의존성·Compose·`application-redis.properties`(프로파일 `redis`) 추가됨.** 앱은 `redis` 프로파일 시에만 해당 설정 로드 |
| 2A-2 | R2: `RedisCacheManager`, TTL·직렬화 정책, (선택) 프로파일별 `ConcurrentMap` 분기 | **`redis`:** Spring Boot 캐시 자동구성(`spring.cache.type=redis`, `spring.cache.redis.*`) + JDK 직렬화(`EventSummaryDto` Serializable). **`!redis`:** 인메모리 |
| 2A-3 | `@Cacheable` 범위: 최소 `getFeedAvailableEvents` → 필요 시 `searchEvents` / `getAllEvents` 키·TTL 설계 | 문서와 범위 일치. Redis 캐시 경로는 `redis` 프로파일 + Compose로 **수동 스모크** 권장 |
| 2A-4 | `@CacheEvict`: 이벤트 생성·수정·삭제 시 무효화 대상 캐시 이름 표로 고정 | **`availableEvents` 전체 무효화:** `EventCreateService`(생성 2경로), `EventUpdateService`(수정 2경로), `EventDeleteService`, `EventStatusService`(전체·단건 상태 갱신) |
| 2A-5 | Phase 2-A 종료: Cache miss → DB → 재적재 흐름 점검 | After 2차 메모 |

---

## Phase 2-B — 투표 수 원자 연산 (문서 2단계 · 투표)

**목표:** 증감 빈번·동시성 — Redis INCR/DECR 또는 동등한 원자 전략(문서와 용어 통일).

| 단위 | 내용 | 종료 시 확인 |
|------|------|----------------|
| 2B-0 | 전제 문서화: SoT(DB vs Redis+DB), 장애 시 복구 한 줄 | 팀 합의 |
| 2B-1 | DB: `(event_id, voter_id)` 유니크 등 중복 투표 방지 | 동시 요청 테스트 |
| 2B-2 | Redis: 키 설계, 투표/취소 INCR/DECR, 조회 순서 | 부하 시 숫자 튐 없음 |
| 2B-3 | DB↔Redis 동기화 방식 1가지 확정(배치·sync API·이중 기록 등) | 정합성 시나리오 |
| 2B-4 | Phase 2-B 종료: k6 등으로 동시성 스모크 | After 3차 메모 |

---

## Phase 3 — Result 정합

- Phase 1~2에서 **실제로 적용한 것만** Result 문장·수치에 반영.
- Problem의 수치(예: 2.3초)는 **실측으로 교체 가능할 때만** 기입.

---

## Phase 4 — 트러블슈팅 (별도 문서 · 작업 완료 후)

이 단계는 **코드 변경 없이** 문서만 작성합니다.

- 증상·재현·기대 vs 실제·원인·해결·롤백
- 설정 스냅샷(Redis, TTL, 캐시 이름, 키 규칙)
- 함정 메모(페이집+fetchJoin, 직렬화, 스탬피드 등)

파일명 예: `docs/event-performance-troubleshooting.md` (작성 시기는 Phase 3 이후)

---

## 권장 진행 순서

### 기술 작업만

`Phase 1` → `Phase 2-A` → `Phase 2-B` → `Phase 3` → `Phase 4`

의존: 2B는 2A와 독립으로 진행 가능하나, **운영·배포 복잡도**를 줄이려면 2A 직후에 2B를 두는 편이 관리하기 쉽습니다.

### 기술 + 포트폴리오(이미지) 병행 권장 순서

1. **1-1** 대상 API·도구 확정 → **Before 캡처·수치** 먼저 확보(없으면 After만으로는 설득이 약해짐).
2. **1-2 ~ 1-3** 적용할 때마다 **동일 시나리오 After** 캡처 → **Before/After 한 쌍** 폴더에 모으기.
3. **1-4**에서 표로 묶은 뒤, 슬라이드용으로 **한 장 요약** 이미지 제작.
4. **2A**는 인프라(Compose)·설정 일부·Redis 키·캐시 효과(2A-5) 순으로 **과정 사진** 추가.
5. **Phase 3**에 실제 수치 반영 후, **Result 전용 슬라이드 이미지** 확정.
6. **Phase 4**는 트러블 1건당 **짧은 스토리 + 캡처**로 보강.
