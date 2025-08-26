# FindInn - 호텔 예약 플랫폼

## 📋 프로젝트 개요

FindInn은 Spring Boot 기반의 종합적인 호텔 예약 플랫폼입니다. 사용자들이 호텔을 검색하고 예약할 수 있으며, 호텔 관리자들이 자신의 호텔을 관리할 수 있는 웹 애플리케이션입니다.

### 🎯 주요 기능

#### 👥 사용자 기능
- **호텔 검색 및 조회**: 지역, 날짜, 인원수 기반 호텔 검색
- **실시간 예약 시스템**: 체크인/체크아웃 날짜 선택 및 실시간 객실 가용성 확인
- **결제 시스템**: 포트원(PortOne) 연동을 통한 안전한 결제 처리
- **리뷰 시스템**: 별점 및 텍스트 리뷰 작성, 이미지 업로드 지원
- **실시간 채팅**: 호텔과의 실시간 문의 채팅
- **쿠폰 시스템**: 할인 쿠폰 적용 기능
- **SMS 인증**: CoolSMS를 통한 휴대폰 인증
- **소셜 로그인**: 네이버,카카오오 OAuth2 로그인 지원

#### 🏨 호텔 관리자 기능
- **호텔 등록 및 관리**: 호텔 정보, 객실 타입, 가격 등록
- **예약 관리**: 예약 현황 조회 및 관리
- **채팅 관리**: 고객 문의 실시간 응답
- **매니저 신청**: 일반 사용자에서 매니저 권한 신청

#### 👨‍💼 시스템 관리자 기능
- **회원 관리**: 전체 회원 조회 및 관리
- **매니저 승인**: 매니저 신청 승인/거절
- **호텔 관리**: 등록된 호텔 전체 조회 및 관리
- **Q&A 관리**: 고객 문의 관리

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Security**: Spring Security 6
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle

### Frontend
- **UI Framework**: Bootstrap 5.3.3
- **JavaScript**: Vanilla JS, jQuery
- **Real-time Communication**: WebSocket (STOMP)
- **Icons**: Font Awesome 6.5.2

### External Services
- **Payment**: PortOne (아임포트)
- **SMS**: CoolSMS
- **OAuth2**: Naver Login
- **Cloud**: AWS RDS (MySQL)

### Development Tools
- **Lombok**: 코드 간소화
- **DevTools**: 개발 편의성 향상
- **JUnit**: 테스트 프레임워크

## 🏗 프로젝트 구조

```
FindInn/
├── src/main/java/com/inn/
│   ├── config/           # 설정 클래스들
│   ├── controller/       # MVC 컨트롤러
│   ├── data/            # 엔티티, DTO, Repository
│   │   ├── booking/     # 예약 관련
│   │   ├── chat/        # 채팅 관련
│   │   ├── coupon/      # 쿠폰 관련
│   │   ├── hotel/       # 호텔 관련
│   │   ├── member/      # 회원 관련
│   │   ├── payment/     # 결제 관련
│   │   ├── review/      # 리뷰 관련
│   │   └── rooms/       # 객실 관련
│   ├── service/         # 비즈니스 로직
│   └── scheduler/       # 스케줄러
├── src/main/resources/
│   ├── static/          # 정적 리소스
│   │   ├── css/         # 스타일시트
│   │   ├── javascript/  # 자바스크립트
│   │   └── image/       # 이미지 파일
│   └── templates/       # Thymeleaf 템플릿
└── build.gradle         # 빌드 설정
```

## 🚀 설치 및 실행

### 필수 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Gradle 7.0 이상

## 📱 주요 페이지

- **메인 페이지**: `/` - 호텔 검색 및 추천
- **호텔 상세**: `/domestic-accommodations/{id}` - 호텔 상세 정보
- **예약 페이지**: `/booking` - 예약 및 결제
- **마이페이지**: `/mypage` - 예약 내역, 리뷰 관리
- **관리자 페이지**: `/admin/**` - 시스템 관리
- **매니저 페이지**: `/manage/**` - 호텔 관리

## 🔧 주요 기능 상세

### 예약 시스템
- 실시간 객실 가용성 확인
- 중복 예약 방지
- 예약 상태 관리 (PENDING → CONFIRMED → COMPLETED)
- 자동 예약 취소 (미결제 예약)

### 결제 시스템
- PortOne 연동
- 결제 검증 및 취소
- 결제 실패 시 자동 환불
- 다양한 결제 수단 지원

### 채팅 시스템
- WebSocket 기반 실시간 채팅
- 호텔별 채팅방 관리
- 채팅 내역 저장 및 조회

### 리뷰 시스템
- 별점 평가 (1-5점)
- 텍스트 리뷰 작성
- 이미지 업로드 지원
- 리뷰 통계 제공

## 🎓 개선사항

### 🔒 보안 강화
- **입력 검증**: 더 엄격한 입력값 검증 및 XSS 방지

### 🏗 아키텍처 개선
- **마이크로서비스**: 모놀리식에서 마이크로서비스로 분리

### 📊 데이터 관리
- **데이터베이스 최적화**: 인덱스 추가 및 쿼리 최적화

### 🎨 사용자 경험
- **반응형 디자인**: 모바일 최적화 개선

### 🔧 개발 환경
- **Docker**: 컨테이너화 및 오케스트레이션

## 📄 라이선스

이 프로젝트는 교육 목적으로 개발되었습니다.

## 👥 개발팀

- **공찬혁**: 팀장, Security, 로그인 및 회원가입 처리, 마이 페이지, 호텔 관리자, 시스템 관리자 기능, WebSocket을 통한 실시간 채팅 설계 및 구현
- **노일권**: 결제 및 예약 시스템, SMS를 통한 인증 시스템 설계 및 구현
- **박현규**: 리뷰 시스템, 호텔 추가, 예약 시스템, 검색 기능 설계 및 구현
- **박호준**: 검색 기능 설계 및 구현, 카카오맵 API 관리
- **원주희**: 쿠폰 발급 및, 이벤트 페이지 설계 및 구현

---

**FindInn** - 편리한 호텔 예약의 시작
