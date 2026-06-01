# 성능/부하 테스트 환경 가이드 (minsujeong)

## 환경 개요
- **목적**: Spring Boot 프로젝트의 성능/부하 테스트
- **도구**: nGrinder (부하 테스트) + Scouter (APM 모니터링)
- **OS**: macOS (Apple Silicon / arm64)

---

## 경로 정리

| 도구 | 경로 |
|---|---|
| Scouter 서버 | `/Users/minsujeong/util/scouter/server/` |
| Scouter 에이전트 | `/Users/minsujeong/util/scouter/agent.java/scouter.agent.jar` |
| Scouter 설정 | `/Users/minsujeong/util/scouter/server/conf/scouter.conf` |
| Scouter 클라이언트 앱 | `/Users/minsujeong/util/scouter.client.app` |
| nGrinder 컨트롤러 | `/Users/minsujeong/ngrinder/ngrinder-controller-3.5.9-p1.war` |
| nGrinder 홈 | `/Users/minsujeong/ngrinder/ngrinder-controller-home/` |
| nGrinder 에이전트 | `/Users/minsujeong/ngrinder/` (run_agent.sh) |
| MySQL (시스템) | `/usr/local/mysql/` |
| 프로젝트 | `/Users/minsujeong/Desktop/프로젝트 모음집/피드샵/Project/FeedShop_Backend/` |
| 환경변수 | `.env.dev` (프로젝트 루트) |

---

## 포트 정리

| 서비스 | 포트 | 비고 |
|---|---|---|
| Spring Boot | **8081** | SERVER_PORT=8081 (.env.dev) |
| nGrinder 컨트롤러 | 8300 | |
| Scouter 서버 | 6100 | |
| MySQL | 3306 | |
| Redis | 6379 | Docker redis-stack-compose, pw: systempass |
| RedisInsight (GUI) | 8001 | http://localhost:8001 |

---

## 프로파일 정리

| 프로파일 | 용도 |
|---|---|
| `dev` | 로컬 개발 (기본값, OAuth2 없이 X-User-Id 헤더 인증) |
| `redis` | Redis 캐시 활성화 (dev와 함께 사용: `dev,redis`) |
| `prod` | 운영 환경 (OAuth2, JWT, GCP) |
| `perf` | nGrinder 부하 테스트용 대량 데이터 초기화 |

---

## 시작 명령어

### 1. MySQL 시작
```bash
sudo /usr/local/mysql/support-files/mysql.server start
```
- 비밀번호: Mac 로그인 비밀번호 (sudo)
- DB 계정: root / Root1234!
- DB 이름: shopgram

### 2. Redis 시작 (Docker)
```bash
# 이미 실행 중인지 확인
docker ps | grep redis-stack

# 미실행 시 시작
docker start redis-stack-compose

# 연결 확인
docker exec redis-stack-compose redis-cli -a systempass ping
```
- 접속 정보: localhost:6379 / password: systempass
- GUI: http://localhost:8001 (RedisInsight)

### 3. Scouter 서버 시작
```bash
cd /Users/minsujeong/util/scouter/server
./startup.sh
```

### 4. Scouter 클라이언트 앱 실행
```bash
open /Users/minsujeong/util/scouter.client.app
```
- 처음 실행 시 quarantine 오류 발생하면:
```bash
xattr -cr /Users/minsujeong/util/scouter.client.app
```

### 5. nGrinder 컨트롤러 시작
```bash
cd /Users/minsujeong/ngrinder
java -jar ngrinder-controller-3.5.9-p1.war -p 8300 -nh /Users/minsujeong/ngrinder/ngrinder-controller-home > /tmp/ngrinder-controller.out &
```
- 접속: http://localhost:8300 (admin / admin)

### 6. nGrinder 에이전트 시작
```bash
cd /Users/minsujeong/ngrinder
./run_agent.sh
```

### 7. Spring Boot 실행 (기본 — dev 프로파일)
```bash
cd "/Users/minsujeong/Desktop/프로젝트 모음집/피드샵/Project/FeedShop_Backend"
set -a && source .env.dev && set +a
./gradlew bootRun
```
- 포트: 8081
- 프로파일: `dev` (application.properties에 `spring.profiles.active=dev` 설정됨)
- 인증: `X-User-Id` 헤더로 loginId 전달 (없으면 dev-user)

### 7-1. Spring Boot 실행 (Redis 캐시 포함)
```bash
cd "/Users/minsujeong/Desktop/프로젝트 모음집/피드샵/Project/FeedShop_Backend"
set -a && source .env.dev && set +a
SPRING_PROFILES_ACTIVE=dev,redis ./gradlew bootRun
```

### 7-2. Spring Boot 실행 (Scouter 에이전트 포함)
```bash
cd "/Users/minsujeong/Desktop/프로젝트 모음집/피드샵/Project/FeedShop_Backend"
set -a && source .env.dev && set +a
./gradlew bootRun -Dspring-boot.run.jvmArguments="\
  -javaagent:/Users/minsujeong/util/scouter/agent.java/scouter.agent.jar \
  -Dscouter.config=/Users/minsujeong/util/scouter/server/conf/scouter.conf \
  -Dobj_name=feedshop \
  --add-opens java.base/java.lang=ALL-UNNAMED"
```

---

## 종료 명령어

### 전체 종료
```bash
# Spring Boot 종료
pkill -f "gradlew bootRun"
pkill -f "FeedShopApplication"

# nGrinder 컨트롤러 종료
pkill -f "ngrinder-controller"

# nGrinder 에이전트 종료
pkill -f "NGrinderAgentStarter"

# Scouter 서버 종료
pkill -f "scouter-server-boot"

# Scouter 클라이언트 종료
pkill -f "scouter.client.app"

# Redis (Docker 컨테이너 유지, 프로세스만 정지)
docker stop redis-stack-compose

# MySQL 종료
sudo /usr/local/mysql/support-files/mysql.server stop
```

### 포트별 프로세스 확인 및 종료
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :<포트번호> | grep LISTEN

# PID로 종료
kill <PID>
```

---

## 전체 실행 상태 확인
```bash
lsof -i :8081 | grep LISTEN   # Spring Boot
lsof -i :8300 | grep LISTEN   # nGrinder
lsof -i :6100 | grep LISTEN   # Scouter 서버
lsof -i :3306 | grep LISTEN   # MySQL
lsof -i :6379 | grep LISTEN   # Redis
ps aux | grep -E "scouter|ngrinder" | grep -v grep
docker ps | grep redis
```

---

## nGrinder 시나리오 (성능 테스트)

### 대상 API 우선순위

| 우선 | HTTP | 경로 | 인증 |
|---|---|---|---|
| P0 | GET | `/api/events/feed-available` | 공개 |
| P0 | GET | `/api/events/search` | 공개 |
| P0 | GET | `/api/events/all` | 공개 |
| P2 | POST | `/api/feeds/{feedId}/vote` | X-User-Id 헤더 |
| P2 | GET | `/api/feeds/{feedId}/vote/count` | 공개 |

### 시나리오 구성

| 시나리오 | 대상 | Vuser | Duration | 목적 |
|---|---|---|---|---|
| A | GET /api/events/all | 10 / 30 / 50 | 각 2분 | QueryDSL + Redis 캐시 Before/After |
| B | GET /api/feeds/{feedId}/vote/count | 10 / 30 / 50 | 각 2분 | Redis 캐시 Before/After |
| C | POST /api/feeds/{feedId}/vote | 동시 50명 | - | TOCTOU → DB 유니크 동시성 테스트 |

---

## 트러블슈팅

### Scouter 서버 실행 시 "Can't lock the database"
```bash
rm /Users/minsujeong/util/scouter/server/database/lock.dat
./startup.sh
```

### MySQL brew services 실행 안 될 때 (Input/output error)
- Homebrew MySQL은 권한 문제로 실행 불가
- `/usr/local/mysql` (시스템 설치) 사용:
```bash
sudo /usr/local/mysql/support-files/mysql.server start
```

### MySQL root 비밀번호 재설정
```bash
sudo /usr/local/mysql/support-files/mysql.server stop
sudo /usr/local/mysql/bin/mysqld_safe --skip-grant-tables --skip-networking --datadir=/usr/local/mysql/data &
sleep 3 && /usr/local/mysql/bin/mysql -u root --socket=/tmp/mysql.sock
```
MySQL 접속 후:
```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Root1234!';
EXIT;
```

### 8081 포트 이미 사용 중
```bash
lsof -i :8081 | grep LISTEN
kill <PID>
```

### Redis 인증 오류 (NOAUTH)
```bash
# 비밀번호 확인
docker exec redis-stack-compose redis-cli -a systempass ping
```

### Spring Boot macOS 앱 "손상됨" 오류
```bash
xattr -cr <앱경로>
```

---

## 참고
- nGrinder 접속: http://localhost:8300 (admin/admin)
- Scouter 클라이언트 서버 연결: localhost:6100
- RedisInsight GUI: http://localhost:8001
- Redis 접속 정보: localhost:6379 / password: systempass
- MySQL study_db 백업 위치: `/Users/minsujeong/util/study_db_notice.sql`
- MySQL 전체 백업 위치: `/Users/minsujeong/util/mysql_backup_all.sql`

## Ehcache 설정 (Spring Boot)

### pom.xml 의존성
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>
```

### @Cacheable 사용 예시
```java
@Cacheable(value = "캐시이름")  // ehcache.xml의 name과 일치해야 함
public List<Object> findAll() { ... }

@Cacheable(value = "캐시이름", key = "#request.requestURI + '-' + #page", condition = "#page <= 5")
public List<Object> findByPage(HttpServletRequest request, int page) { ... }
```
