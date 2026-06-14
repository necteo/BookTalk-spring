# BookTalk — Backend

도서 소셜 플랫폼 BookTalk의 Spring Boot 백엔드 서버입니다.

> Frontend 레포: [BookTalk-react](https://github.com/necteo/BookTalk-react)

## 기술 스택

- Java, Spring Boot 3.x
- Spring Security, JWT (HttpOnly Cookie)
- OAuth2 소셜 로그인 (Google / Kakao / Naver)
- Spring AI + Google Gemini (`gemini-2.5-flash`)
- JPA / Hibernate, MySQL

---

## 주요 기능

| 기능 | 설명 |
|---|---|
| 도서 API | 메인 페이지 데이터, 목록(페이지네이션), 상세 |
| 리뷰 API | 리뷰 작성 / 수정 / 삭제 |
| 인증 | OAuth2 소셜 로그인, JWT 발급/재발급/로그아웃 |
| AI 챗봇 | Gemini 기반 챗봇 |

---

## 인증 설계

### JWT — HttpOnly Cookie
localStorage 대신 HttpOnly 쿠키에 토큰을 저장해 XSS로부터 보호합니다.

| 토큰 | 만료 | 경로 |
|---|---|---|
| Access Token | 15분 | `/` |
| Refresh Token | 7일 | `/api/auth` |

Refresh Token Rotation을 적용해 재발급 시 기존 토큰을 폐기하고 새 토큰을 발급합니다.

### OAuth2 로그인 흐름

```
소셜 로그인 요청
    → Spring Security OAuth2 필터
    → CustomOAuth2UserService (회원 저장/업데이트)
    → OAuth2AuthenticationSuccessHandler
        → JWT 발급
        → HttpOnly 쿠키 설정
        → React 앱으로 리다이렉트
```

### Spring Security 필터 구조

```
요청
 └─▶ JwtAuthenticationFilter
      ├─ WHITELIST(/api/book, /api/comment, /api/auth 등) → 파싱 스킵
      └─ 그 외 → 토큰 파싱 → SecurityContext 설정 (만료/오류면 401)
 └─▶ SecurityConfig (authorizeHttpRequests)
      ├─ /api/book/**, /api/comment/**, /api/auth/**, /oauth2/** → permitAll
      ├─ /api/admin/** → ADMIN
      └─ 그 외(/api/member/me 포함) → authenticated
```

`JwtAuthenticationFilter`는 토큰 파싱과 인증 설정만 담당하고,
인가 제어는 `SecurityConfig`에서 일원화해 책임을 분리했습니다.

`/api/member/me`는 WHITELIST·permitAll에서 제외해, 토큰이 없거나 만료되면 **401**을 반환합니다.
프런트는 이 401을 받아 자동으로 토큰을 재발급(refresh)합니다.

### 토큰 재발급 / 자동 로그인 흐름

```
요청 (Access Token 만료)
    → 401
    → 프런트 Axios 인터셉터가 /api/auth/refresh 호출
        ├─ Refresh Token 유효 → 새 Access/Refresh 발급(Rotation) → 원요청 재시도
        └─ Refresh Token 만료/무효 → 비로그인 처리
```

Access Token 쿠키(15분)가 만료돼도 Refresh Token(7일)이 살아있으면 재발급으로 로그인이 유지됩니다.
(refresh 요청 자체는 재시도 대상에서 제외해 무한 루프를 방지)

---

## API 명세

### 도서
| Method | URL | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/book/main` | 메인 페이지 데이터 | 불필요 |
| GET | `/api/book/list/{page}` | 도서 목록 | 불필요 |
| GET | `/api/book/detail/{isbn}` | 도서 상세 + 리뷰 | 불필요 |

### 리뷰
| Method | URL | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/comment/insert` | 리뷰 작성 | 필요 |
| PUT | `/api/comment/update` | 리뷰 수정 (본인 글만) | 필요 |
| DELETE | `/api/comment/delete/{no}` | 리뷰 삭제 (본인 글만) | 필요 |

### 인증
| Method | URL | 설명 |
|---|---|---|
| POST | `/api/auth/refresh` | Access Token 재발급 |
| DELETE | `/api/auth/logout` | 로그아웃 (쿠키 삭제) |
| GET | `/api/member/me` | 내 정보 조회 (미인증 시 401 → refresh 트리거) |

### AI 챗봇
| Method | URL | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/chat/stream?message=` | AI 챗봇 응답 | 필요 |

---

## 프로젝트 구조

```
src/main/java/com/sist/web/
├── controller/     # REST API 컨트롤러
├── service/        # 비즈니스 로직
├── repository/     # JPA Repository
├── entity/         # JPA 엔티티 (Member, Comment, WikiBook, RefreshToken)
├── dto/            # DTO / Record
├── config/         # SecurityConfig, CorsConfig
├── filter/         # JwtAuthenticationFilter
├── handler/        # OAuth2AuthenticationSuccessHandler/FailureHandler
├── token/          # JwtTokenProvider
├── info/           # OAuth2UserInfo (Google/Kakao/Naver)
├── exception/      # CustomException, ErrorCode, BookExceptionHandler
└── util/           # CookieUtil
```

---

## 실행 방법

```bash
# application.yml 설정 필요
# spring.datasource.url / username / password
# jwt.secret / jwt.expiration / jwt.refresh-expiration
# spring.security.oauth2.client.registration.google/kakao/naver
# spring.ai.google.genai.api-key
# oauth2.redirect-url

./gradlew bootRun
```
