# WalkIt Backend - OAuth2 + JWT Authentication

Google과 Naver OAuth2 로그인을 통합한 Spring Boot 백엔드 애플리케이션입니다.

## 주요 기능

- Google OAuth2 로그인
- Naver OAuth2 로그인
- JWT 토큰 기반 인증
- Refresh Token을 통한 토큰 갱신
- `@AuthenticationPrincipal`을 통한 사용자 정보 접근

## 기술 스택

- Java 17
- Spring Boot 4.0
- Spring Security 6.x
- Spring Data JPA
- MySQL 8.x
- JWT (JJWT 0.12.5)
- Lombok

## 시작하기

### 1. 환경 변수 설정

`.env.example` 파일을 참고하여 환경 변수를 설정합니다.

```bash
# .env.example을 복사하여 실제 값으로 설정
cp .env.example .env
```

필요한 환경 변수:
- `DB_USERNAME`: MySQL 사용자 이름
- `DB_PASSWORD`: MySQL 비밀번호
- `JWT_SECRET`: JWT 서명에 사용할 시크릿 키 (최소 32자)
- `GOOGLE_CLIENT_ID`: Google OAuth2 클라이언트 ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth2 클라이언트 시크릿
- `NAVER_CLIENT_ID`: Naver OAuth2 클라이언트 ID
- `NAVER_CLIENT_SECRET`: Naver OAuth2 클라이언트 시크릿
- `ALLOWED_ORIGINS`: CORS 허용 오리진 (프론트엔드 URL)

### 2. MySQL 데이터베이스 생성

```sql
CREATE DATABASE walkit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. OAuth2 제공자 설정

#### Google Cloud Console
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성
3. "API 및 서비스" > "사용자 인증 정보" > "사용자 인증 정보 만들기" > "OAuth 2.0 클라이언트 ID"
4. 애플리케이션 유형: 웹 애플리케이션
5. 승인된 리디렉션 URI 추가:
   - `http://localhost:8080/oauth2/callback/google`
   - 프로덕션 URL (배포 시)
6. 클라이언트 ID와 시크릿 복사

#### Naver Developers
1. [Naver Developers](https://developers.naver.com/) 접속
2. "애플리케이션" > "애플리케이션 등록"
3. 사용 API: 네이버 로그인
4. 제공 정보: 이메일, 이름, 프로필 사진 선택
5. 서비스 URL: `http://localhost:8080`
6. Callback URL: `http://localhost:8080/oauth2/callback/naver`
7. 클라이언트 ID와 시크릿 복사

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 IDE에서 `WalkitApplication.java` 실행

## API 엔드포인트

### 인증 관련

- **OAuth2 로그인 시작**
  - Google: `GET /oauth2/authorization/google`
  - Naver: `GET /oauth2/authorization/naver`

- **토큰 갱신**
  ```
  POST /api/auth/refresh
  Content-Type: application/json

  {
    "refreshToken": "your-refresh-token"
  }
  ```

- **로그아웃**
  ```
  POST /api/auth/logout
  Authorization: Bearer {access-token}
  Content-Type: application/json

  {
    "refreshToken": "your-refresh-token"
  }
  ```

- **현재 사용자 정보 조회**
  ```
  GET /api/auth/me
  Authorization: Bearer {access-token}
  ```

### 예제 엔드포인트

- **보호된 리소스 접근**
  ```
  GET /api/example/protected
  Authorization: Bearer {access-token}
  ```

- **POST 요청 예제**
  ```
  POST /api/example/create-something
  Authorization: Bearer {access-token}
  Content-Type: application/json

  {
    "key": "value"
  }
  ```

## 인증 흐름

### 1. OAuth2 로그인
```
사용자 → /oauth2/authorization/{provider}
→ OAuth 제공자 인증 페이지
→ 사용자 승인
→ /oauth2/callback/{provider}
→ JWT 토큰 발급
→ 프론트엔드 리디렉트 (토큰 포함)
```

### 2. API 요청
```
클라이언트 → API 요청 (Authorization: Bearer {JWT})
→ JwtAuthenticationFilter
→ 토큰 검증
→ UserPrincipal 생성
→ SecurityContext 설정
→ 컨트롤러 실행 (@AuthenticationPrincipal 사용 가능)
```

### 3. 토큰 갱신
```
클라이언트 → POST /api/auth/refresh (refreshToken)
→ RefreshToken 검증
→ 새 Access Token 발급
→ 새 Refresh Token 발급 (선택적)
```

## 컨트롤러에서 사용자 정보 접근

```java
@RestController
@RequestMapping("/api/your-domain")
public class YourController {

    @PostMapping
    public ResponseEntity<?> createSomething(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody YourRequest request) {

        // 사용자 정보 접근
        Long userId = userPrincipal.getId();
        String email = userPrincipal.getEmail();
        String name = userPrincipal.getName();
        Role role = userPrincipal.getRole();

        // 비즈니스 로직 수행
        // ...

        return ResponseEntity.ok(response);
    }
}
```

## 프론트엔드 연동

### 로그인 버튼
```javascript
// Google 로그인
const handleGoogleLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};

// Naver 로그인
const handleNaverLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/naver';
};
```

### 리디렉트 처리
```javascript
// /oauth2/redirect 페이지에서
useEffect(() => {
  const params = new URLSearchParams(window.location.search);
  const token = params.get('token');
  const refreshToken = params.get('refreshToken');

  if (token && refreshToken) {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('refreshToken', refreshToken);
    navigate('/dashboard');
  }
}, []);
```

### API 요청
```javascript
// Axios 설정
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

// 요청 인터셉터 - 토큰 자동 추가
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 응답 인터셉터 - 토큰 갱신
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');

      try {
        const response = await axios.post('/api/auth/refresh', { refreshToken });
        const { accessToken, refreshToken: newRefreshToken } = response.data.data;

        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        error.config.headers['Authorization'] = `Bearer ${accessToken}`;
        return axios(error.config);
      } catch (refreshError) {
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

## 데이터베이스 스키마

### users 테이블
- `id`: 사용자 고유 ID (PK)
- `email`: 이메일 (UNIQUE)
- `name`: 이름
- `profile_image_url`: 프로필 이미지 URL
- `provider`: OAuth 제공자 (GOOGLE, NAVER)
- `provider_id`: OAuth 제공자의 사용자 ID
- `role`: 사용자 역할 (USER, ADMIN)
- `created_at`, `updated_at`: 생성/수정 시간

### refresh_tokens 테이블
- `id`: 토큰 ID (PK)
- `user_id`: 사용자 ID (FK)
- `token`: Refresh Token (UNIQUE)
- `expires_at`: 만료 시간
- `created_at`: 생성 시간

## 보안 고려사항

1. **JWT Secret**: 최소 256비트(32자) 이상의 안전한 랜덤 문자열 사용
2. **HTTPS**: 프로덕션 환경에서는 반드시 HTTPS 사용
3. **Refresh Token**: 데이터베이스에 저장하여 관리 및 취소 가능
4. **CORS**: 신뢰할 수 있는 오리진만 허용
5. **토큰 만료**: Access Token 1시간, Refresh Token 7일 (조정 가능)

## 문제 해결

### 토큰 서명 검증 실패
- `JWT_SECRET` 환경 변수가 올바르게 설정되었는지 확인
- 최소 32자 이상인지 확인

### OAuth2 리디렉트 에러
- Google/Naver 콘솔에서 리디렉트 URI가 정확히 일치하는지 확인
- `http://localhost:8080/oauth2/callback/{provider}` 형식 확인

### CORS 에러
- `ALLOWED_ORIGINS` 환경 변수에 프론트엔드 URL이 포함되어 있는지 확인

## 라이선스

MIT License
