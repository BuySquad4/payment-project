# 💳 Payment Project

> **PortOne V2 기반 풀스택 결제 플랫폼**  
> Spring Boot 4.0 + Thymeleaf + JWT + 포인트/멤버십 시스템

---

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [결제 플로우 아키텍처](#결제-플로우-아키텍처)
- [주요 기능](#주요-기능)
- [API 엔드포인트](#api-엔드포인트)
- [Webhook 처리 플로우](#webhook-처리-플로우)
- [환경 설정](#환경-설정)
- [실행 방법](#실행-방법)
- [개발자 가이드](#개발자-가이드)
- [FAQ](#faq)

---

## 📌 프로젝트 개요

백엔드 API와 Thymeleaf 프론트엔드가 함께 제공되는 **결제·주문·포인트 풀스택 애플리케이션**입니다.  
부트캠프 학습용으로 설계되어 end-to-end 결제 플로우를 한 번에 경험할 수 있습니다.

### 핵심 특징

| 특징 | 설명 |
|------|------|
| **풀스택 제공** | 인증·상품·주문·결제·환불·웹훅·포인트·멤버십 백엔드 + Thymeleaf UI |
| **API 계약 기반** | `client-api-config.yml` 정의 → 프론트가 `/api/public/config`로 자동 로드 |
| **PortOne SDK 연동** | 결제창·빌링키(프론트) + 결제 조회·취소·웹훅(백엔드) |
| **3가지 독립 결제 플로우** | 기본 결제 / 포인트 결제 / 구독 결제(UI·계약만, 백엔드 미구현) |

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.2 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| DB | MySQL |
| Template | Thymeleaf |
| Build | Gradle 9.3 |
| 결제 | PortOne V2 API + Browser SDK |
| 기타 | Lombok, Jackson, Actuator, Scheduling |

---

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/bootcamp/paymentproject/
│   │   ├── PaymentProjectApplication.java
│   │   ├── common/            # Security, Config, Exception, Properties, DataInitializer
│   │   ├── user/              # AuthController, AuthService, User
│   │   ├── product/           # 상품 CRUD
│   │   ├── order/             # 주문 생성·조회
│   │   ├── payment/           # 결제 생성·확정·(실패 시 자동 환불)
│   │   ├── refund/            # 환불 (RefundService, PaymentRefundService)
│   │   ├── portone/           # PortOne API 클라이언트 (getPayment, cancelPayment)
│   │   ├── webhook/           # PortOne 웹훅 수신·처리
│   │   ├── point/             # 포인트 적립·사용·스케줄러
│   │   └── membership/        # 등급, UserMembership
│   └── resources/
│       ├── application.yml
│       ├── client-api-config.yml     # ⭐ API 계약 정의 (프론트 연동 핵심)
│       ├── static/
│       │   ├── css/style.css
│       │   └── js/
│       │       ├── app-config.js     # 런타임 설정 로더
│       │       ├── api-handler.js    # API 호출 헬퍼
│       │       ├── api-validator.js  # API 응답 검증
│       │       ├── portone-sdk.js    # PortOne SDK 래퍼
│       │       ├── auth-check.js     # 인증 체크
│       │       ├── theme.js          # 다크모드
│       │       └── cookie-util.js    # 쿠키 유틸
│       └── templates/
│           ├── layout.html, home.html
│           ├── login.html, register.html
│           ├── shop.html, orders.html, points.html
│           └── plans.html, subscribe.html, subscriptions.html
└── test/
```

---

## 🔄 결제 플로우 아키텍처

이 프로젝트는 **3가지 독립적인 결제 플로우**를 제공합니다.

| 플로우 | 페이지 | 포인트 사용 | 확정 방식 | 백엔드 |
|--------|--------|:-----------:|:---------:|:------:|
| 기본 결제 | 주문 (`/orders`) | ❌ | 수동 | ✅ 구현 |
| 포인트 결제 | 포인트 (`/points`) | ✅ | 자동 | ✅ 구현 |
| 구독 결제 | 구독 (`/subscribe`) | ❌ | - | ⚠️ UI만 |

### 1️⃣ 기본 결제 (일반 카드)

```
상점 → 주문 생성(create-order)
     → 주문 페이지 → 결제 생성(create-payment)
     → PortOne 결제창 → 결제 완료
     → 사용자가 "결제 확정" 클릭 → confirm-payment
```

### 2️⃣ 포인트 결제

```
포인트 페이지 → 주문 조회(list-orders) → 주문 선택
             → 포인트 입력 → 결제 생성(create-payment, pointsToUse 포함)
             → PortOne 결제창(finalAmount로 결제)
             → 결제 완료 시 자동으로 confirm-payment 호출
```

### 3️⃣ 구독 결제 (UI·계약만 제공)

```
플랜 선택 → 빌링키 발급(PortOne.requestIssueBillingKey)
          → 구독 생성(create-subscription)
          → 정기 청구 실행(create-billing)
```

> **참고:** 구독·빌링키·청구 백엔드 API는 현재 **미구현**이며, UI와 `client-api-config.yml` 스키마만 제공됩니다.

---

## ✨ 주요 기능

### 🔐 인증 (JWT)
- 회원가입 / 로그인 / 현재 사용자 조회
- JWT 토큰 발급 및 검증 (`Authorization: Bearer {token}` 또는 쿠키)

### 🛍 상품 / 주문
- 상품 목록·상세 조회
- 주문 생성 — **서버에서 금액 계산** (클라이언트 위변조 방지)
- 주문 목록 조회

### 💳 결제 (PortOne V2 연동)
- 결제 생성 (`PENDING` 상태로 DB 선등록)
- 결제 확정 (PortOne API로 금액 검증 후 상태 업데이트)
- 포인트 차감 결제 (`pointsToUse` 파라미터)
- 결제 취소 / 환불

### 🪝 Webhook 처리
- PortOne V2 Webhook 수신 및 서명 검증
- **멱등 처리** — `webhook-id` UNIQUE 제약 + 애플리케이션 레벨 이중 방어
- **상태 전이 검증** — `REFUNDED → PAID` 등 잘못된 전이 차단
- **비관적 락** — 동시 결제 환경에서 재고 초과 차감 방지
- 환불 시 포인트 복구 (CANCEL 트랜잭션 생성)

### 🪙 포인트
- 멤버십 등급별 적립률 적용
- 결제 승인 시 `HOLDING` 적립 → `PointScheduler`가 `EARN`으로 확정
- 환불 시 적립 포인트 회수 및 사용 포인트 복구

### 🥇 멤버십
- 등급별 포인트 적립률 관리
- 사용자-멤버십 연결

---

## 🌐 API 엔드포인트

### 인증

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) |
| GET | `/api/auth/me` | 현재 사용자 정보 조회 |

### 상품

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/products` | 상품 목록 조회 |
| GET | `/api/products/{id}` | 상품 상세 조회 |

### 주문

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/orders` | 주문 생성 |
| GET | `/api/orders` | 주문 목록 조회 (현재 로그인 사용자) |

### 결제

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/payments` | 결제 생성 (PENDING 등록) |
| POST | `/api/payments/{paymentId}/confirm` | 결제 확정 |
| POST | `/api/payments/{paymentId}/refund` | 결제 취소 / 환불 |

### Webhook

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/portone-webhook` | PortOne Webhook 수신 |

### 구독 (UI·계약만, 백엔드 미구현)

| Method | URL | 설명 |
|--------|-----|------|
| GET | *(미정의)* | 구독 플랜 목록 |
| POST | *(미정의)* | 구독 생성 |
| GET | *(미정의)* | 구독 조회 |
| PATCH | *(미정의)* | 구독 상태 업데이트 (해지) |
| POST | *(미정의)* | 즉시 청구 실행 |

---

## 🪝 Webhook 처리 플로우

```
[PortOne] POST /portone-webhook
    │
    ├── 1. 서명 검증 (webhook-signature 헤더)
    ├── 2. DTO 파싱 (PortoneWebhookPayload)
    ├── 3. PortOne API로 실제 결제 상태 재조회 (SSOT)
    │
    └── 4. DB 트랜잭션 (WebhookTxService)
            ├── 멱등 처리: 중복 webhookId → 즉시 무시
            ├── webhook_event 테이블에 RECEIVED 기록
            ├── 상태 전이 유효성 검증
            │
            ├── APPROVED  → 재고 차감(비관적 락) + 주문 완료 + HOLDING 포인트 적립
            ├── FAILED    → 결제 실패 처리
            ├── CANCELED  → 결제 취소 + 주문 환불
            ├── REFUNDED  → 결제 환불 + 포인트 회수 + 사용 포인트 복구
            └── 처리 결과 → webhook_event.status → PROCESSED / FAILED
```

---

## ⚙️ 환경 설정

### application.yml

```yaml
spring:
  config:
    import: optional:file:./secret.yml   # 민감 정보는 secret.yml에 분리

portone:
  api:
    base-url: https://api.portone.io
    secret: ${PORTONE_API_SECRET:}
  webhook:
    secret: demo-webhook-secret-key
    secret-format: raw
```

### secret.yml *(프로젝트 루트, .gitignore 추가 권장)*

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/payment_db
    username: your_db_user
    password: your_db_password

jwt:
  secret: your_jwt_secret_key

portone:
  store:
    id: your_store_id
  channel: kg-inicis          # 일반 결제용 채널
  api:
    secret: your_portone_api_secret

app:
  ui:
    branding:
      appName: Payment Project
```

### 채널 분리

| 채널 | 용도 |
|------|------|
| `kg-inicis` | 일반 결제 (주문, 포인트) |
| `toss` | 구독 결제 — 빌링키 발급 |

### API 계약 설정 (`client-api-config.yml`)

프론트엔드가 `/api/public/config`를 통해 이 파일을 읽어 모든 API를 호출합니다.  
**`url` 필드가 없으면 해당 API는 호출되지 않으며, 화면에 자동으로 경고가 표시됩니다.**

```yaml
api:
  base-url: http://localhost:8080
  endpoints:
    create-order:
      url: /api/orders
      method: POST
      description: 주문 생성
      request:
        fields:
          - name: items
            type: array
            required: true
      response:
        body:
          fields:
            - name: orderId
              type: string
              required: true

    confirm-payment:
      url: /api/payments/{paymentId}/confirm   # Path Parameter 예시
      method: POST
      pathParams:
        - name: paymentId
          description: 결제 ID
```

---

## 🚀 실행 방법

### 1. MySQL DB 생성

```sql
CREATE DATABASE payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 설정 파일 작성

프로젝트 루트에 `secret.yml` 생성 후 DB 정보 및 시크릿 키 입력

### 3. 빌드 & 실행

```bash
# macOS / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

### 4. 브라우저 접속

```
http://localhost:8080
```

### 5. 로그인

회원가입(`/pages/register`) 후 로그인하거나, 아래 시드 계정을 사용하세요.

```
이메일: admin@test.com
비밀번호: admin
```

> `DataInitializer`가 기동 시 관리자 계정·상품·멤버십 등을 자동 생성합니다.

---

## 🔧 개발자 가이드

### JavaScript API 호출

**설정 로드**

```javascript
const config = await getConfig();
console.log(config.portone.storeId);
console.log(config.api.baseUrl);
```

**API URL 생성**

```javascript
// 일반 URL
const url = await buildApiUrl('create-order');
// → http://localhost:8080/api/orders

// Path Parameter 포함
const url = await buildApiUrl('confirm-payment', { paymentId: 'pay_123' });
// → http://localhost:8080/api/payments/pay_123/confirm
```

**API 호출**

```javascript
const result = await makeApiRequest('create-order', {
  method: 'POST',
  body: { items: [{ productId: 'prod_001', quantity: 2 }] }
});
if (!result.success) throw new Error(result.error);
console.log('Order ID:', result.data.orderId);
```

**PortOne SDK**

```javascript
// 기본 결제 (포인트 없음, 수동 확정)
await openPortOnePayment({
  paymentId: 'pay_123',
  orderName: '상품명',
  totalAmount: 50000,
  currency: 'KRW',
  payMethod: 'CARD',
  customer: { customerId, fullName, email, phoneNumber }
});

// 포인트 결제 (자동 확정)
await openPortOnePaymentWithPoints({
  paymentId: 'pay_123',
  totalAmount: 50000,
  pointsToUse: 5000,
  currency: 'KRW',
  payMethod: 'CARD',
  customer: { ... }
});

// 빌링키 발급 (구독)
await issuePortOneBillingKey({
  issueId: 'issue_001',
  issueName: '정기결제 등록',
  customer: { ... }
});
```

### API 구현 체크리스트

**필수 (기본 결제)**
- [x] `POST /api/auth/signup` — 회원가입
- [x] `POST /api/auth/login` — 로그인
- [x] `GET /api/auth/me` — 현재 사용자 (customerUid, email, name, phone, pointBalance)
- [x] `GET /api/products` — 상품 목록
- [x] `POST /api/orders` — 주문 생성
- [x] `POST /api/payments` — 결제 생성
- [x] `POST /api/payments/{paymentId}/confirm` — 결제 확정
- [x] `POST /api/payments/{paymentId}/refund` — 결제 취소

**선택 (포인트 결제)**
- [ ] `GET /api/orders` — 주문 목록 (PENDING 상태 조회용)
- [ ] `create-payment`에 `pointsToUse` 파라미터 처리

**선택 (구독 결제, 미구현)**
- [ ] `create-subscription`, `get-subscription`, `update-subscription`
- [ ] `create-billing`, `list-billing-history`

---

## ❓ FAQ

**Q. `client-api-config.yml`에 URL이 없으면?**  
해당 API는 호출되지 않고, 화면에 경고가 표시됩니다. 각 엔드포인트에 `url`, `method`를 반드시 추가하세요.

**Q. 주문 페이지와 포인트 페이지가 왜 분리되어 있나요?**  
기본 결제(포인트 없음, 수동 확정)와 포인트 결제(포인트 포함, 자동 확정)를 독립적으로 테스트하기 위함입니다.

**Q. `create-order`에 `pointsToUse`가 필요한가요?**  
아니요. 포인트는 주문 생성이 아닌 **`create-payment` 요청**에서만 `pointsToUse`로 전달합니다.

**Q. `get-current-user`는 언제 호출하나요?**  
PortOne 결제창 / 빌링키 발급 **직전**에 한 번 호출합니다. 응답의 `customerUid`는 서버에서 관리하는 고유 식별자이므로 프론트에서 임의로 생성하지 마세요.

**Q. `base-url`을 변경하고 싶어요.**  
`client-api-config.yml`의 `api.base-url`을 수정하면 모든 API 호출에 반영됩니다.

**Q. CORS 에러가 발생해요.**  
`SecurityConfig`에 CORS 허용 설정을 추가하세요.

---

## 📚 참고 자료

- [PortOne 개발자 문서](https://docs.portone.io/)
- [PortOne SDK v2](https://docs.portone.io/v2)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Thymeleaf 공식 문서](https://www.thymeleaf.org/)

---

## 📝 라이선스

이 프로젝트는 **교육 목적**으로 제공됩니다.  
버그 리포트나 개선 제안은 이슈로 등록해 주세요.

---

> 💡 **Tip:** `client-api-config.yml`의 각 엔드포인트에 `url` 필드를 추가하면 프론트엔드가 자동으로 해당 API를 호출합니다!
