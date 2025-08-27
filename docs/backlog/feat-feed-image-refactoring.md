# User Story: 피드 이미지 업로드 기능 개선

## As a [사용자], I want to [목표] so that [이유/가치].

**As a** 피드를 작성하는 사용자,  
**I want to** 피드 작성 시 이미지를 쉽게 업로드하고 관리할 수 있도록 하고,  
**so that** 더 풍부한 콘텐츠로 피드를 작성할 수 있고, 이미지 관리가 편리해진다.

**As a** 피드를 수정하는 사용자,  
**I want to** 기존 이미지를 삭제하고 새로운 이미지를 추가할 수 있도록 하고,  
**so that** 피드 내용을 더 정확하게 표현할 수 있고, 이미지 업데이트가 자유롭다.

**As a** 피드를 삭제하는 사용자,  
**I want to** 피드 삭제 시 관련된 이미지 파일도 함께 정리되도록 하고,  
**so that** 불필요한 파일이 서버에 남지 않고 저장 공간을 효율적으로 관리할 수 있다.

## Description

피드 도메인에서 이미지 업로드 기능을 개선하여 사용자가 더 편리하게 이미지를 관리할 수 있도록 합니다. 기존의 단순한 URL 기반 이미지 저장 방식에서 실제 파일 업로드 방식으로 변경하고, 이미지 검증, 저장, 삭제 기능을 통합적으로 제공합니다.

### 주요 개선사항
- **이미지 업로드**: MultipartFile을 통한 실제 파일 업로드 지원
- **이미지 검증**: 파일 크기, 형식, 개수 제한 등 검증 로직 추가
- **이미지 관리**: 피드 수정 시 이미지 추가/삭제 기능
- **자동 정리**: 피드 삭제 시 관련 이미지 파일 자동 삭제
- **환경 분리**: 개발/운영 환경에 따른 이미지 저장 방식 차별화
- **API 문서화**: Swagger를 통한 완전한 API 문서 제공

## Acceptance Criteria (완료 조건)

### 피드 생성 관련
- [x] 피드 생성 시 이미지 파일 업로드 가능 (multipart/form-data)
- [x] 이미지 없이 텍스트만으로도 피드 생성 가능
- [x] 이미지 파일 검증 (크기, 형식, 개수 제한)
- [x] 이미지 파일을 스토리지에 저장하고 DB에 경로 저장
- [x] 개발/운영 환경에 따른 이미지 저장 방식 분리

### 피드 수정 관련
- [x] 피드 수정 시 기존 이미지 삭제 가능
- [x] 피드 수정 시 새로운 이미지 추가 가능
- [x] 이미지 수정 없이 텍스트만 수정 가능
- [x] 삭제된 이미지 파일을 스토리지에서 실제 삭제

### 피드 삭제 관련
- [x] 피드 삭제 시 관련된 모든 이미지 파일 자동 삭제
- [x] 이미지 삭제 실패 시에도 피드 삭제는 정상 진행
- [x] 삭제 실패 로그 기록

### 이미지 관리 관련
- [x] FeedImageService를 통한 이미지 업로드/삭제 로직 분리
- [x] StorageService 인터페이스를 통한 환경별 저장 방식 추상화
- [x] ImageValidator를 통한 이미지 파일 검증
- [x] UploadDirectory enum에 FEEDS 추가

### API 및 문서화
- [x] 피드 생성/수정 API에서 multipart/form-data 지원
- [x] 기존 JSON API와의 호환성 유지
- [x] Swagger 문서를 통한 API 명세 제공
- [x] 별도 Swagger 문서 클래스로 API 문서화

### 테스트 관련
- [x] FeedImageService 단위 테스트 작성
- [x] FeedCreateService 이미지 업로드 테스트 추가
- [x] FeedUpdateService 이미지 수정 테스트 추가
- [x] FeedDeleteService 이미지 삭제 테스트 추가
- [x] 모든 테스트 케이스 통과

### 코드 품질
- [x] 기존 코드와의 호환성 유지
- [x] 예외 처리 및 에러 메시지 개선
- [x] 로깅 추가
- [x] 코드 리뷰 준비 완료

## 구현을 위한 기술 Task (To-Do)

### 1. 인프라스트럭처 개선
- [x] UploadDirectory enum에 FEEDS 추가
- [x] StorageService 인터페이스 확인 및 활용
- [x] ImageValidator 컴포넌트 활용

### 2. 도메인 서비스 구현
- [x] FeedImageService 클래스 생성
- [x] 이미지 업로드 메서드 구현 (uploadImages)
- [x] 이미지 삭제 메서드 구현 (deleteImages)
- [x] 이미지 교체 메서드 구현 (replaceImages)

### 3. 애플리케이션 서비스 수정
- [x] FeedCreateService에 FeedImageService 의존성 추가
- [x] createFeedWithImages 메서드 구현
- [x] FeedUpdateService에 FeedImageService 의존성 추가
- [x] updateFeedWithImages 메서드 구현
- [x] FeedDeleteService에 이미지 삭제 로직 추가

### 4. 컨트롤러 수정
- [x] FeedCreateController에 multipart/form-data 엔드포인트 추가
- [x] FeedUpdateController에 multipart/form-data 엔드포인트 추가
- [x] 기존 JSON API 엔드포인트 유지

### 5. DTO 수정
- [x] FeedUpdateRequestDto에 deleteImageIds, newImageUrls 필드 추가
- [x] 기존 필드들과의 호환성 유지

### 6. 테스트 코드 작성
- [x] FeedImageServiceTest 클래스 생성
- [x] FeedCreateServiceTest에 이미지 업로드 테스트 추가
- [x] FeedUpdateServiceTest에 이미지 수정 테스트 추가
- [x] FeedDeleteServiceTest에 이미지 삭제 테스트 추가
- [x] Mock 객체 설정 및 검증 로직 구현

### 7. API 문서화
- [x] FeedSwaggerDocumentation 클래스 생성
- [x] 모든 피드 관련 API 문서 작성
- [x] 요청/응답 예시 및 에러 코드 명세

### 8. 빌드 및 배포
- [x] 컴파일 오류 해결
- [x] 테스트 통과 확인
- [x] 코드 커밋 및 푸시

## 기술적 세부사항

### 이미지 저장 방식
- **개발환경**: MockStorageService 사용, CDN URL 형태로 반환
- **운영환경**: GcpStorageService 사용, GCS에 실제 파일 저장

### 이미지 검증 규칙
- 최대 파일 크기: 10MB
- 최대 이미지 개수: 10개
- 허용 파일 형식: JPEG, PNG, GIF, WebP

### API 엔드포인트
- `POST /api/feeds` (multipart/form-data) - 이미지 포함 피드 생성
- `POST /api/feeds/text-only` (application/json) - 텍스트만 피드 생성
- `PUT /api/feeds/{feedId}` (multipart/form-data) - 이미지 포함 피드 수정
- `PUT /api/feeds/{feedId}/text-only` (application/json) - 텍스트만 피드 수정

### 파일 구조
```
src/main/java/com/cMall/feedShop/feed/
├── application/
│   ├── service/
│   │   ├── FeedImageService.java (신규)
│   │   ├── FeedCreateService.java (수정)
│   │   ├── FeedUpdateService.java (수정)
│   │   └── FeedDeleteService.java (수정)
│   └── dto/
│       └── request/
│           └── FeedUpdateRequestDto.java (수정)
├── presentation/
│   ├── FeedCreateController.java (수정)
│   ├── FeedUpdateController.java (수정)
│   └── FeedSwaggerDocumentation.java (신규)
└── domain/
    └── entity/
        └── FeedImage.java (기존)
```

## 관련 이슈
- **지라 이슈**: MYCE-177
- **브랜치**: MYCE-177-refactor-feed-ImageRefactoring-api
- **관련 도메인**: Feed, Storage, Image

## 완료된 작업
- [x] 피드 이미지 업로드 기능 구현
- [x] 피드 이미지 수정/삭제 기능 구현
- [x] 이미지 검증 및 에러 처리
- [x] 환경별 저장 방식 분리
- [x] 테스트 코드 작성
- [x] API 문서화
- [x] 빌드 테스트 통과
- [x] 코드 커밋 및 푸시
