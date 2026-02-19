# 💳 Payment Project - 결제/구독 풀스택

부트캠프용 **풀스택 결제 프로젝트** - Spring Boot + Thymeleaf + PortOne (백엔드 API + 프론트엔드 UI)

---

## 📋 프로젝트 개요

이 프로젝트는 **백엔드 API와 프론트엔드 UI가 함께 제공**되는 결제·주문·포인트 풀스택 애플리케이션입니다.

### 핵심 특징

- ✅ **풀스택 제공** - 인증·상품·주문·결제·환불·웹훅·포인트·멤버십 백엔드 API 구현 포함
- ✅ **API 계약 기반** - `client-api-config.yml`에서 API 계약 정의, 프론트는 `/api/public/config`로 로드
- ✅ **PortOne SDK 연동** - 결제창/빌링키(프론트) + 결제 조회·취소·웹훅(백엔드)
- ✅ **3가지 독립적인 결제 플로우**
  - **기본 결제**: 일반 카드 결제 (주문 페이지)
  - **포인트 결제**: 포인트 사용 결제 (포인트 페이지)
  - **구독 결제**: 빌링키 기반 정기결제 (구독 페이지, UI·계약만, 백엔드 미구현)

### 왜 이 프로젝트를 사용하나요?

| 목적 | 설명 |
|------|------|
| **풀스택 학습** | 백엔드 API와 Thymeleaf 프론트가 함께 있어 end-to-end 결제 플로우를 한 번에 경험 |
| **실전 결제 플로우** | PortOne SDK + 서버 결제 확정/취소/웹훅으로 실제 결제 프로세스 이해 |
| **유연한 API 계약** | YML 수정만으로 API URL·필드 설계 변경 가능, 프론트가 자동 반영 |
| **흐름별 테스트** | 기본 결제 / 포인트 결제 / 구독(UI) 3가지 흐름을 각각 독립적으로 테스트 |

---

## 🎯 결제 플로우 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                     3가지 독립적인 결제 플로우                        │
└─────────────────────────────────────────────────────────────────┘
```

**1️⃣ 기본 결제 (일반 카드 결제)**  
상점 → 주문 생성 → 주문 페이지 → 결제 시작 → PortOne 결제창 → 결제 확정

**2️⃣ 포인트 결제 (포인트 사용)**  
포인트 페이지 → 주문 조회 → 주문 선택 → 포인트 입력 → 결제 → 자동 확정

**3️⃣ 구독 결제 (정기결제)**  
플랜 선택 → 빌링키 발급 → 구독 생성 → 정기 청구 실행 *(백엔드 미구현, UI·계약만)*

### 왜 분리되었나?

각 결제 플로우는 서로 다른 비즈니스 요구사항을 가집니다.

| 플로우 | 페이지 | 포인트 사용 | 확정 방식 | 주요 사용처 |
|--------|--------|-------------|-----------|-------------|
| 기본 결제 | 주문 | ❌ | 수동 | 일반 쇼핑몰 결제 |
| 포인트 결제 | 포인트 | ✅ | 자동 | 포인트 할인 결제 |
| 구독 결제 | 구독 | ❌ | - | 정기 결제 (멤버십, 구독) |

---

## 🚀 빠른 시작

### 1. 프로젝트 실행

```bash
./gradlew build
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

### 2. 브라우저 접속

```
http://localhost:8080
```

### 3. 로그인

- 회원가입(`/pages/register`) 후 로그인(`/pages/login`)
- **시드 데이터**: `DataInitializer`가 기동 시 관리자 계정·상품·멤버십 등을 생성합니다.  
  **기본 테스트 계정: admin@test.com / admin**
- JWT는 응답 헤더 `Authorization: Bearer {token}` 또는 쿠키로 전달됩니다.

---

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/bootcamp/paymentproject/
│   │   ├── PaymentProjectApplication.java      # Spring Boot 메인
│   │   ├── common/                             # Security, Config, Exception, Properties, DataInitializer
│   │   ├── user/                               # AuthController, AuthService, User
│   │   ├── product/                            # 상품 CRUD
│   │   ├── order/                              # 주문 생성·조회
│   │   ├── payment/                            # 결제 생성·확정·(실패 시 자동 환불)
│   │   ├── refund/                             # 환불 (RefundService, PaymentRefundService)
│   │   ├── portone/                            # PortOne API (getPayment, cancelPayment)
│   │   ├── webhook/                            # PortOne 웹훅 수신·처리
│   │   ├── point/                              # 포인트, 스케줄러
│   │   └── membership/                         # 등급, UserMembership
│   └── resources/
│       ├── application.yml
│       ├── client-api-config.yml               # API 계약 정의 (중요!)
│       ├── static/
│       │   ├── css/style.css
│       │   └── js/
│       │       ├── app-config.js               # 런타임 설정 로더
│       │       ├── api-handler.js              # API 호출 헬퍼
│       │       ├── api-validator.js            # API 응답 검증
│       │       ├── portone-sdk.js              # PortOne SDK 래퍼
│       │       ├── theme.js                    # 다크모드
│       │       ├── auth-check.js               # 인증 체크
│       │       └── cookie-util.js              # 쿠키 유틸
│       └── templates/
│           ├── layout.html, home.html
│           ├── login.html, register.html
│           ├── shop.html, orders.html, points.html
│           ├── plans.html, subscribe.html, subscriptions.html
│           └── ...
└── test/
    └── java/...
```

---

## ⚙️ 설정 가이드

### 1. PortOne 설정

**application.yml**  
`portone.api.base-url`, `portone.api.secret`(또는 환경변수 `PORTONE_API_SECRET`),  
`portone.webhook.secret`(대시보드 웹훅 시크릿과 동일), `secret-format: raw`

**secret.yml** (프로젝트 루트, .gitignore)

- `portone.store.id`, `portone.channel`(예: kg-inicis, toss)
- DB: `spring.datasource.url`, `username`, `password`
- 선택: `app.ui.branding` (appName, tagline, logoText)

**주의사항**

- **kg-inicis**: 일반 결제(주문, 포인트)에 사용
- **toss**: 구독 결제(빌링키 발급)에 사용
- 환경변수 활용 권장: `PORTONE_API_SECRET`, `PORTONE_STORE_ID`

### 2. API 계약 설정 (가장 중요!)

`src/main/resources/client-api-config.yml`에서 API 계약을 정의합니다. 프론트는 `/api/public/config`로 이 설정을 읽어 호출합니다.

**기본 구조**

```yaml
api:
  base-url: http://localhost:8080

  endpoints:
    create-order:
      url: /api/orders              # ⬅️ 엔드포인트 경로 (필수!)
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
```

**URL 필드**  
각 API에는 `url` 필드가 **필수**입니다. URL이 없으면 프론트엔드가 해당 API를 호출할 수 없습니다.

**Path Parameter**  
URL에 `{paramName}` 형태로 경로 파라미터를 정의할 수 있습니다.

```yaml
confirm-payment:
  url: /api/payments/{paymentId}/confirm
  method: POST
  pathParams:
    - name: paymentId
      description: 결제 ID
```

JavaScript 예시:

```javascript
const url = await buildApiUrl('confirm-payment', { paymentId: 'pay_123' });
// 결과: http://localhost:8080/api/payments/pay_123/confirm
```

### 3. 동적 API 목록 표시

각 페이지는 필요한 API 목록을 `client-api-config.yml`에서 동적으로 읽어 화면에 표시합니다.

- ✅ **정의됨**: 화면에 API 정보 표시, 호출 가능
- ❌ **미정의**: 경고 메시지 표시 (url 필드 추가 필요)

---

## 📖 사용 가이드

### 1️⃣ 기본 결제 플로우 (포인트 없음)

**목적:** 일반적인 쇼핑몰 결제 (포인트 사용 없음)

**Step 1: 상품 선택 및 주문 생성**

- 상점 페이지 이동 → 상품 수량 선택 → "주문 생성" 클릭
- ✅ **API:** `create-order`  
  요청: `{ items: [{ productId, quantity }] }`  
  응답: `{ orderId, totalAmount, orderNumber }`

**Step 2: 결제 및 확정**

- 주문 페이지 이동 → 주문번호로 조회
- ✅ **API:** `get-current-user` → 응답의 customerUid, email, name, phone은 PortOne 결제창 `customer`에 사용
- "결제 시작" 클릭 → ✅ **API:** `create-payment` (요청: orderId, totalAmount / 응답: paymentId)
- ✅ **SDK:** PortOne.requestPayment() (결제창) → 카드 입력 후 결제 완료
- "결제 확정" 클릭 → ✅ **API:** `confirm-payment` (요청: paymentId / 응답: success, status)

**필요 API:** list-products, create-order, get-current-user, create-payment, confirm-payment, cancel-payment(경로: `/api/payments/{id}/refund`)

---

### 2️⃣ 포인트 결제 플로우 (포인트 포함)

**목적:** 포인트를 사용한 할인 결제

**Step 1: 주문 조회 및 선택**

- 포인트 페이지 이동
- ✅ **API:** `list-orders` (PENDING 주문 조회) → 결제할 주문 선택

**Step 2: 포인트 입력 및 결제**

- ✅ **API:** `get-current-user` (결제창 customer용)
- 사용 포인트 입력 → "결제 진행" 클릭
- ✅ **API:** `create-payment`  
  요청: `{ orderId, totalAmount, pointsToUse }`  
  응답: `{ paymentId, finalAmount }`
- ✅ **SDK:** PortOne.requestPayment() → **finalAmount**로 결제
- 결제 완료 시 ✅ **자동:** `confirm-payment` 호출 (화면에 진행 상황 표시)

**필요 API:** list-orders, get-current-user, create-payment(pointsToUse 포함), confirm-payment

**차이점:** 포인트 페이지는 결제 후 **자동 확정**, create-payment에 `pointsToUse` 전달

---

### 3️⃣ 구독 결제 플로우 (정기결제)

**목적:** 빌링키 기반 정기 결제 (멤버십, 구독)

- 플랜 페이지 → 플랜 선택 → "구독 신청하기" → 구독 신청 페이지
- ✅ **API:** `get-current-user` (빌링키 발급에 필요)
- ✅ **SDK:** PortOne.requestIssueBillingKey() (카드 등록) → 응답: billingKey, customerUid
- ✅ **API:** `create-subscription` (요청: customerUid, planId, billingKey, amount / 응답: subscriptionId)
- 구독 관리 페이지: get-subscription, create-billing, list-billing-history

**참고:** 구독·빌링키·청구 백엔드 API는 현재 **미구현**이며, UI와 client-api-config.yml 스키마만 제공됩니다.

---

## 🔍 API 구현 체크리스트

**필수 API (기본 결제)**

- **인증:** login, register, get-current-user
- **상품·주문:** list-products, create-order
- **결제:** create-payment, confirm-payment, cancel-payment (경로: `/api/payments/{paymentId}/refund`)

**선택 API (포인트 결제)**

- list-orders

**선택 API (구독 결제, 미구현)**

- create-subscription, get-subscription, update-subscription, create-billing, list-billing-history

---

## 💡 핵심 개념

### 1. API 계약 기반 개발 (Contract-First)

`client-api-config.yml`이 **단일 진실 공급원(Single Source of Truth)** 입니다.

- 프론트엔드는 이 파일(서버에서는 `/api/public/config`로 전달)만 보고 API를 호출합니다.
- URL, 필드명, 타입을 YML에서 관리하고, API가 없으면 화면에 자동으로 경고가 표시됩니다.

### 2. 도메인 분리 (Domain Separation)

- **Order** → 주문 생성·조회
- **Payment** → 결제 처리·확정
- **Point** → 포인트 적립·사용
- **Refund** → 환불 (14일 이내, PortOne 취소 후 포인트 복구)
- **Subscription** → 구독·정기결제 (현재 UI·계약만)

주문 생성에는 포인트가 없고, 포인트는 **결제 시작(create-payment)** 에서만 `pointsToUse`로 전달합니다.

### 3. 결제 플로우 분리 (자동 vs 수동 확정)

| 함수 | 페이지 | 포인트 | 확정 방식 | 사용 시나리오 |
|------|--------|--------|-----------|---------------|
| openPortOnePayment() | 주문 | ❌ | 수동 | 일반 결제 |
| openPortOnePaymentWithPoints() | 포인트 | ✅ | 자동 | 포인트 할인 결제 |

- **수동 (주문 페이지):** 결제 완료 → 사용자가 "결제 확정" 클릭 → confirm-payment 호출
- **자동 (포인트 페이지):** 결제 완료 → 자동으로 confirm-payment 호출 → 화면에 진행 상황 표시

### 4. 채널 분리 (Channel Separation)

- **kg-inicis**: 일반 결제 (주문, 포인트) — 카드 정보 매번 입력
- **toss**: 정기 결제 (빌링키 발급) — 빌링키로 자동 결제

### 5. 다크모드 & 동적 API 경고

- **다크모드:** 우측 상단 🌙 버튼으로 토글, `localStorage`에 저장 (새로고침 유지)
- **동적 API 경고:** 각 페이지는 필요한 API가 `client-api-config.yml`에 정의되어 있는지 표시합니다. ❌ 미정의 시: url 추가 필요 안내

---

## 🔧 개발자 가이드

### 기술 스택

| 구분 | 기술 |
|------|------|
| 백엔드 | Java 17, Spring Boot 4.0.2, Web, Data JPA, Security, Validation |
| DB | MySQL |
| 인증 | JWT (jjwt 0.12.5), BCrypt |
| 프론트 | Thymeleaf, 정적 JS/CSS, PortOne Browser SDK v2 |
| 기타 | Lombok, Jackson, Actuator, Scheduling |

### JavaScript API 호출

**1. 설정 로드**

```javascript
const config = await getConfig();
console.log(config.portone.storeId);
console.log(config.api.baseUrl);
```

**2. API URL 생성**

```javascript
const url = await buildApiUrl('create-order');
// 결과: http://localhost:8080/api/orders

const url = await buildApiUrl('confirm-payment', { paymentId: 'pay_123' });
// 결과: http://localhost:8080/api/payments/pay_123/confirm
```

**3. API 호출**

```javascript
const result = await makeApiRequest('create-order', {
  method: 'POST',
  body: {
    items: [{ productId: 'prod_001', quantity: 2 }]
  }
});
if (!result.success) throw new Error(result.error);
console.log('Order ID:', result.data.orderId);
```

**4. PortOne SDK**

```javascript
// 기본 결제 (포인트 없음)
await openPortOnePayment({
  paymentId: 'pay_123',
  orderName: '상품명',
  totalAmount: 50000,
  currency: 'KRW',
  payMethod: 'CARD',
  customer: { customerId, fullName, email, phoneNumber }
});

// 포인트 결제
await openPortOnePaymentWithPoints({
  paymentId: 'pay_123',
  totalAmount: 50000,
  pointsToUse: 5000,
  currency: 'KRW',
  payMethod: 'CARD',
  customer: { ... }
});

// 빌링키 발급
await issuePortOneBillingKey({
  issueId: 'issue_001',
  issueName: '정기결제 등록',
  customer: { ... }
});
```

---

## ❓ FAQ

**Q1. client-api-config.yml에 URL이 없으면?**  
해당 API는 호출되지 않습니다. 각 엔드포인트에 `url`, `method`를 반드시 추가하세요.

**Q2. 주문 페이지와 포인트 페이지가 왜 분리되어 있나요?**  
기본 결제(포인트 없음, 수동 확정)와 포인트 결제(포인트 포함, 자동 확정)를 **독립적으로** 테스트하기 위함입니다.

**Q3. create-order API에 pointsToUse가 필요한가요?**  
아니요. 주문 생성에는 포인트가 없습니다. 포인트는 **create-payment** 요청에서만 `pointsToUse`로 보냅니다.

**Q4. get-current-user는 언제 호출하나요?**  
PortOne 결제창/빌링키 발급 **전에** 한 번. 결제창 `customer`(customerUid, email, name, phone)에 필요합니다. customerUid는 서버에서 관리하는 고유 식별자이므로 프론트에서 임의로 생성하지 마세요.

**Q5. base-url을 변경하려면?**  
`client-api-config.yml`의 `api.base-url`을 수정하세요. 모든 API 호출이 이 주소를 사용합니다.

**Q6. CORS 에러가 나요.**  
백엔드에서 CORS를 허용하세요. `SecurityConfig`에 설정 추가(TODO인 경우).

---

## 📚 참고 자료

**PortOne**  
[PortOne 개발자 문서](https://docs.portone.io/), [PortOne SDK v2](https://docs.portone.io/v2), 빌링키 발급 가이드

**Spring Boot & Thymeleaf**  
[Spring Boot 공식 문서](https://spring.io/projects/spring-boot), [Thymeleaf 공식 문서](https://www.thymeleaf.org/)

---

## 📝 라이선스 & 기여

이 프로젝트는 교육 목적으로 제공됩니다.  
버그 리포트나 개선 제안은 이슈로 등록해 주세요. PR 시 `.github/PULL_REQUEST_TEMPLATE.md` 체크리스트를 참고해 주세요.

---

> 💡 **Tip:** `client-api-config.yml`에 각 API의 `url` 필드를 추가하면 프론트엔드가 자동으로 해당 API를 호출할 수 있습니다!
