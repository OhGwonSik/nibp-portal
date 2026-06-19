# 국가생명윤리정책원 대표홈페이지 (nibp-portal)

공공기관 대표 홈페이지 백엔드. nibp-edu에서 설계한 공통 모듈을 재활용.

---

## 기술 스택

- **Backend**: Spring Boot 2.7.18, eGovFramework 4.3.0, MyBatis
- **DB**: MariaDB
- **Auth**: Spring Security, JWT, Redis
- **기타**: Thymeleaf, Apache POI, JXLS
- **Infra**: Docker, Jenkins

---

## 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 기간 | 2025.10 ~ 2026.03 (약 5개월) |
| 팀 구성 | 3인 |
| 도메인 | 기관 소개 / 정보 제공 / 민원 / 메일링 |

---

## 요구사항

- Java 11 이상
- Gradle (또는 내장된 gradlew 사용)
- MariaDB
- Redis

---

## 실행 방법

```bash
git clone https://github.com/OhGwonSik/nibp-portal.git
cd nibp-portal

# application.yml.example을 참고하여 application.yml 생성 후 값 입력

./gradlew bootRun
```

---

## 주요 기능

### nibp-edu 공통 모듈 재활용
- 게시판 / 설문 / 엑셀 공통 모듈 nibp-portal에 그대로 재활용
- 국내/해외 언론동향 카테고리/게재일자/키워드 컬럼 추가

### 메일링 시스템 신규 개발
- 뉴스레터 신청 / 변경 / 해지 3가지 모드
- AES 암호화 인증코드 발송, 비동기 처리, 만료 처리
- 관리자 알림 이메일 자동 발송
- 만료 인증 데이터 스케줄러 정리

### 공공데이터 의견수렴
- 사용자 의견 저장 + 관리자 조회

### N-gram 통합검색
- 게시판/공지/QNA 전체 콘텐츠 N-gram 기반 통합 검색

---

## 트러블슈팅

### 메일링 인증 코드 발송 실패
- **문제**: MessagingException 발생 시 트랜잭션 롤백 문제
- **해결**: 비동기(sendEmailWithTemplateAsync) 발송으로 분리

### QNA 비밀번호 암호화 불일치
- **문제**: 쿼리단 암호화 미적용으로 비교 실패
- **해결**: BoardMapper.xml 암호화 쿼리로 변경

---

## 환경 설정

민감 정보는 `application.yml`에 관리되며 별도 제공되지 않습니다.

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/nibp_portal
    username:
    password:
  redis:
    host: localhost
    port: 6379
  mail:
    host:
    username:
    password:

jwt:
  secret:

mailing:
  notification:
    receivers:
```
