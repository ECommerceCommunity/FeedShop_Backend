# 🛍️ Pull Request

## 📋 Summary
<!-- 이 PR이 무엇을 하는지 한 줄로 요약해주세요 -->
- 소셜 로그인 기능 추가 및 기존 로그인 로직 개선

**Type**
- [x] ✨ Feature
- [ ] 🐛 Bug Fix
- [x] ♻️ Refactor
- [ ] 🎨 UI/UX
- [ ] 📝 Docs
- [x] 🔧 Chore


---

## 🎯 What & Why
### 무엇을 했나요?
<!-- 구현한 기능이나 수정한 내용을 설명해주세요 -->
- Google, Kakao, Naver를 이용한 소셜 로그인 기능을 구현했습니다.
- 기존 OAuth2 인증 실패 시 에러 메시지 처리 방식을 개선했습니다.
- CI 워크플로우에 소셜 로그인 관련 환경변수를 추가했습니다.

### 왜 필요했나요?
<!-- 이 작업이 필요한 이유나 해결하려는 문제를 설명해주세요 -->
- 사용자에게 다양한 로그인 옵션을 제공하여 편의성을 높이고, 회원가입 장벽을 낮추기 위해 소셜 로그인 기능이 필요했습니다.
- 기존에는 OAuth2 인증 실패 시 구체적인 오류 메시지가 노출되지 않아 사용자와 개발자 모두 원인 파악이 어려웠습니다. 이를 개선하여 명확한 피드백을 제공하고자 했습니다.

---

## 🔧 How (구현 방법)
### 주요 변경사항
- `CustomOAuth2UserService`: Naver, Google, Kakao 등 다양한 OAuth2 제공자로부터 사용자 정보를 가져와 처리하는 로직을 구현했습니다.
- `OAuth2UserInfoFactory`: 각 제공자(Provider)에 맞는 `OAuth2UserInfo` 객체를 생성하는 팩토리 클래스를 구현했습니다.
- `OAuth2AuthenticationFailureHandler`: 인증 실패 시 `Optional`을 사용하여 NullPointerException을 방지하고, 보다 명확한 에러 메시지를 생성하도록 수정했습니다.
- `ci.yml`: Github Actions 워크플로우에 소셜 로그인에 필요한 `CLIENT_ID`와 `CLIENT_SECRET`을 secrets으로 추가했습니다.

### 기술적 접근
- Spring Security의 OAuth2 클라이언트를 활용하여 소셜 로그인을 구현했습니다.
- `DefaultOAuth2UserService`를 상속받아 `CustomOAuth2UserService`를 구현하여 우리 서비스에 맞는 사용자 처리 로직을 추가했습니다.
- 인증 실패 시 `SimpleUrlAuthenticationFailureHandler`를 상속받은 `OAuth2AuthenticationFailureHandler`에서 에러 메시지를 가공하여 리다이렉트 URI의 쿼리 파라미터로 전달하도록 구현했습니다.

---

## 🧪 Testing
### 테스트 방법
<!-- 어떻게 테스트했는지 설명해주세요 -->
- 로컬 환경에서 각 소셜 로그인(Google, Kakao, Naver) 버튼을 클릭하여 정상적으로 로그인이 되는지 확인했습니다.
- 각 소셜 로그인 과정에서 의도적으로 인증을 실패시켜 (e.g., 동의 항목 미체크) 에러 메시지가 정상적으로 프론트엔드로 전달되는지 확인했습니다.
- Postman을 사용하여 소셜 로그인 API의 응답 형식을 검증했습니다.

### 확인 사항
- [x] 기능 정상 동작 확인
- [x] 기존 기능 영향 없음
- [x] 예외 케이스 테스트 완료

---

## 📎 관련 이슈 / 문서
- 관련 이슈: N/A
- 지라 백로그: N/A
---

## 💬 Additional Notes
<!-- 리뷰어가 알아야 할 추가 정보나 주의사항 -->
- 각 소셜 플랫폼에서 발급받은 클라이언트 ID와 시크릿은 Github Secrets에 등록되어 CI/CD 파이프라인에서 안전하게 사용됩니다.
- 리뷰어는 로컬에서 테스트 시 `application.properties` 또는 환경변수에 각 소셜 로그인 관련 키 값을 설정해야 합니다.

---

## ✅ Checklist
- [x] 코드 리뷰 준비 완료
- [x] 테스트 완료
- [x] 불필요한 로그 제거