# 🚀 GCP Cloud Run 자동 배포 설정 가이드

이 문서는 FeedShop Backend 프로젝트의 GCP Cloud Run 자동 배포를 설정하는 방법을 설명합니다.

## 📋 사전 준비사항

### 1. GCP 설정

#### 1.1 필요한 GCP API 활성화
```bash
# Cloud Run API
gcloud services enable run.googleapis.com

# Container Registry API  
gcloud services enable containerregistry.googleapis.com

# Cloud Build API (선택사항)
gcloud services enable cloudbuild.googleapis.com
```

#### 1.2 Service Account 생성 및 권한 설정
```bash
# Service Account 생성
gcloud iam service-accounts create github-actions-sa \
    --description="Service Account for GitHub Actions" \
    --display-name="GitHub Actions SA"

# 필요한 권한 부여
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions-sa@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/run.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions-sa@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/storage.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions-sa@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/iam.serviceAccountUser"

# Service Account Key 생성
gcloud iam service-accounts keys create github-actions-key.json \
    --iam-account=github-actions-sa@$PROJECT_ID.iam.gserviceaccount.com
```

### 2. GitHub Secrets 설정

GitHub 레포지토리의 Settings > Secrets and variables > Actions에서 다음 secrets을 추가하세요:

#### 2.1 GCP 관련 Secrets
| Secret Name | 설명 | 예시 값 |
|-------------|------|---------|
| `GCP_PROJECT_ID` | GCP 프로젝트 ID | `onyx-oxygen-462722-c0` |
| `GCP_SA_KEY` | Service Account JSON 키 전체 내용 | `{"type": "service_account", ...}` |

#### 2.2 데이터베이스 Secrets
| Secret Name | 설명 | 현재 설정값 참고 |
|-------------|------|----------------|
| `DB_NAME` | 데이터베이스 이름 | `application.properties` 참고 |
| `DB_USERNAME` | DB 사용자명 | `application.properties` 참고 |
| `DB_PASSWORD` | DB 비밀번호 | 🔒 보안 정보 |

#### 2.3 애플리케이션 Secrets
| Secret Name | 설명 | 현재 설정값 참고 |
|-------------|------|----------------|
| `JWT_SECRET` | JWT 서명 키 | 🔒 보안 정보 |
| `MAILGUN_API_KEY` | Mailgun API 키 | `application.properties` 참고 |
| `MAILGUN_DOMAIN` | Mailgun 도메인 | `application.properties` 참고 |
| `MAILGUN_EMAIL` | Mailgun 이메일 | `application.properties` 참고 |
| `GCS_ID` | Google Cloud Storage 프로젝트 ID | `application.properties` 참고 |
| `GCS_BUCKET` | GCS 버킷 이름 | `application.properties` 참고 |
| `CDN_BASE_URL` | CDN 기본 URL | `https://cdn-feedshop.store` |
| `RECAPTCHA_SECRET_KEY` | reCAPTCHA 비밀 키 | 🔒 보안 정보 |

## 🔧 배포 프로세스

### 자동 배포 (권장)
1. `main` 브랜치에 코드를 병합하면 자동으로 배포가 시작됩니다
2. GitHub Actions에서 진행 상황을 확인할 수 있습니다

### 수동 배포
1. GitHub Actions 탭으로 이동
2. "Deploy to GCP Cloud Run" workflow 선택
3. "Run workflow" 버튼 클릭

## 📊 모니터링 및 디버깅

### Cloud Run 서비스 확인
```bash
# 서비스 상태 확인
gcloud run services describe feedshop-backend --region=asia-northeast3

# 서비스 URL 확인
gcloud run services describe feedshop-backend --region=asia-northeast3 --format="value(status.url)"

# 로그 확인
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=feedshop-backend" --limit=50
```

### 헬스 체크
배포 완료 후 다음 엔드포인트에서 서비스 상태를 확인할 수 있습니다:
- Health Check: `https://your-service-url/actuator/health`
- API Documentation: `https://your-service-url/swagger-ui/index.html`

## 🔒 보안 고려사항

### 1. Workload Identity Federation (권장)
더 안전한 인증을 위해 Service Account Key 대신 Workload Identity Federation 사용을 권장합니다:

```bash
# Workload Identity Pool 생성
gcloud iam workload-identity-pools create "github-pool" \
    --project="$PROJECT_ID" \
    --location="global" \
    --display-name="GitHub Actions Pool"

# Provider 생성
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
    --project="$PROJECT_ID" \
    --location="global" \
    --workload-identity-pool="github-pool" \
    --display-name="GitHub Actions Provider" \
    --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
    --issuer-uri="https://token.actions.githubusercontent.com"
```

### 2. 환경별 설정 분리
- 개발/스테이징/프로덕션 환경별로 별도의 Secret 설정
- 민감한 정보는 절대 코드에 하드코딩하지 않기

## 🚨 문제 해결

### 일반적인 오류와 해결방법

#### 1. 권한 오류
```
ERROR: (gcloud.run.deploy) User [...] does not have permission to access...
```
**해결:** Service Account에 필요한 권한(`roles/run.admin` 등)이 제대로 부여되었는지 확인

#### 2. 이미지 빌드 실패
```
ERROR: failed to build: unable to prepare context...
```
**해결:** Dockerfile 경로와 build context 확인

#### 3. 서비스 시작 실패
```
ERROR: Container failed to start...
```
**해결:** 
- 애플리케이션 로그 확인
- 환경 변수 설정 확인
- 포트 설정 확인 (8080)

#### 4. 헬스 체크 실패
```
Health check failed
```
**해결:**
- `/actuator/health` 엔드포인트가 활성화되어 있는지 확인
- Spring Boot Actuator 의존성 확인
- 방화벽 설정 확인

## 📈 성능 최적화

### Cloud Run 설정 최적화
현재 CD pipeline에서 사용하는 설정:
- **Memory**: 2Gi (필요에 따라 조정)
- **CPU**: 2 (vCPU)
- **Concurrency**: 1000 (동시 처리 요청 수)
- **Min Instances**: 1 (콜드 스타트 방지)
- **Max Instances**: 10 (비용 제어)

### Docker 이미지 최적화
- Multi-stage build 사용으로 이미지 크기 최소화
- Layer caching을 통한 빌드 시간 단축
- JVM 컨테이너 최적화 옵션 적용

## 📞 지원

배포 관련 문제가 있으면 다음을 확인해 주세요:
1. GitHub Actions 로그
2. GCP Cloud Run 로그
3. 이 문서의 문제 해결 섹션

추가 도움이 필요하면 개발팀에 문의해 주세요.
