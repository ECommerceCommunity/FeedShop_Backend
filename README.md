# 👟 FeedShop | 신발 전문 이커머스 백엔드

[![CI](https://github.com/ECommerceCommunity/FeedShop_Backend/actions/workflows/ci.yml/badge.svg)](https://github.com/ECommerceCommunity/FeedShop_Backend/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ECommerceCommunity_FeedShop_Backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ECommerceCommunity_FeedShop_Backend)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ECommerceCommunity_FeedShop_Backend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=ECommerceCommunity_FeedShop_Backend)

신발 전문 쇼핑몰 FeedShop의 백엔드 API 서버입니다. Spring Boot를 기반으로 쇼핑몰의 핵심 기능을 구현합니다.

- **Frontend Repository**: [FeedShop Frontend (React)](https://github.com/your-frontend-repo) (링크를 실제 프론트엔드 레포지토리로 교체해주세요)

---

## 📚 목차

- [✨ 주요 기능](#-주요-기능)
- [⚙️ 아키텍처](#️-아키텍처)
- [📄 ERD](#-erd)
- [🛠️ 기술 스택](#️-기술-스택)
- [🚀 시작하기](#-시작하기)
- [📖 API 문서](#-api-문서)
- [🧪 테스트](#-테스트)
- [CI/CD](#cicd)
- [🤝 기여 방법](#-기여-방법)
- [📝 라이선스](#-라이선스)

---

## ✨ 주요 기능

-   👤 **사용자 관리**: JWT 기반 인증, 소셜 로그인, 회원 정보 관리
-   🛒 **장바구니**: 상품 추가, 수량 변경, 삭제 기능
-   🛍️ **주문 및 결제**: 상품 주문, 주문 내역 조회, 결제 연동
-   ✍️ **리뷰**: 구매 상품에 대한 리뷰 작성, 조회, 수정, 삭제
-   💰 **포인트 및 쿠폰**: 활동 기반 포인트 적립/사용, 쿠폰 관리
-   👑 **관리자**: 사용자 및 상품 관리를 위한 어드민 기능

---

## ⚙️ 아키텍처

*(여기에 프로젝트 아키텍처 다이어그램을 추가하면 좋습니다. 예: 클린 아키텍처, MSA 구조 등)*

![Architecture Diagram Placeholder](https://via.placeholder.com/800x400.png?text=Project+Architecture+Diagram)

---

## 📄 ERD

*(데이터베이스 관계를 나타내는 ERD(Entity-Relationship Diagram)를 추가하세요.)*

![ERD Placeholder](https://via.placeholder.com/800x500.png?text=Entity-Relationship+Diagram)

---

## 🛠️ 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **Backend** | `Java 17`, `Spring Boot 3.3`, `Spring Data JPA`, `QueryDSL` |
| **Database** | `MySQL`, `H2` (for tests) |
| **Security** | `Spring Security`, `JWT` |
| **API Docs** | `SpringDoc` (Swagger UI) |
| **Build Tool**| `Gradle` |
| **DevOps** | `GitHub Actions`, `Docker` |
| **Code Quality**| `SonarCloud`, `Jacoco` |

---

## 🚀 시작하기

### 사전 요구사항

-   `Java 17`
-   `Gradle 8.x`
-   `MySQL 8.0`

### 설치 및 실행

1.  **레포지토리 클론**
    ```bash
    git clone https://github.com/ECommerceCommunity/FeedShop_Backend.git
    cd FeedShop_Backend
    ```

2.  **application.yml 설정**
    `src/main/resources/application.yml` 파일을 열고, 환경에 맞게 데이터베이스 및 JWT 설정을 수정합니다.
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/feedshop
        username: your-db-username
        password: your-db-password

    jwt:
      secret: your-jwt-secret-key
    ```

3.  **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```

---

## 📖 API 문서

애플리케이션 실행 후, 아래 URL로 접속하여 API 문서를 확인할 수 있습니다.

-   **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui.html)

---

## 🧪 테스트

프로젝트의 전체 테스트를 실행하려면 다음 명령어를 사용하세요.

```bash
./gradlew test
```

테스트 실행 후, 커버리지 리포트는 `build/reports/jacoco/test/html/index.html`에서 확인할 수 있습니다.

---

## CI/CD

본 프로젝트는 GitHub Actions를 사용하여 CI/CD 파이프라인을 자동화합니다.

-   `.github/workflows/ci.yml`: Pull Request 또는 main 브랜치 push 시, 자동으로 빌드, 테스트, 코드 분석을 수행합니다.
-   `.github/workflows/create-jira-issue.yml`: (필요시 Jira 연동 워크플로우에 대한 설명을 추가합니다.)

---

## 🤝 기여 방법

이 프로젝트에 기여하고 싶으시다면, 언제든지 Pull Request를 보내주세요!

1.  레포지토리를 Fork합니다.
2.  새로운 기능 브랜치를 생성합니다. (`git checkout -b feature/AmazingFeature`)
3.  변경 사항을 커밋합니다. (`git commit -m 'Add some AmazingFeature'`)
4.  브랜치에 Push합니다. (`git push origin feature/AmazingFeature`)
5.  Pull Request를 생성합니다.

자세한 내용은 `pull_request_template.md` 파일을 참고해주세요.

---

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 `LICENSE` 파일을 참고하세요.
