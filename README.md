<img width="200" height="200" alt="logo" src="https://github.com/user-attachments/assets/e0b2a2ab-72ab-44c0-a4e9-0330420f0c4c" />

# 코드지니 프로젝트
- __프로젝트 명__ : 코드지니 (CodeGenie)

- __프로젝트 소개__ : 코딩테스트 학습 도우미 서비스, 코드지니

- __프로젝트 목표__ : 사용자가 입력한 학습 주제를 바탕으로 요약본과 예상문제 등을 자동 생성, 사용자 맞춤 코딩테스트 학습 지원 서비스 제공

- __주요 기능__ :
  - 로그인/회원가입
  - 로그아웃
  - AI 문제 생성 (10문제)
  - 문제 해설 및 개념 카드 제공
  - 문제 제출
  - 마음에 드는 문제 및 해설&개념 북마크

- __개발 기간__ : 2025.10.01 ~ 2025.10.21 (약 3주간)

- __프로젝트 블로그__ : [노션_코드지니 (CodeGenie)](https://www.notion.so/27e83a6b63ea8184a6f2d8f6736c70fc)

- __발표 자료__ : [Canva_프로젝트 PPT]()

- __시연 영상__ : <영상 추가>
<br>



<!-- --------------------------------------------------------------------------------------------------------------- -->
<!--팀원 소개-->
## Team "알라딘"
&nbsp;&nbsp;&nbsp;&nbsp;KOSA_3차 프로젝트 팀 알라딘

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: 사용자가 학습을 희망하는 부분에 대한 맞춤 코딩테스트 문제를 알라딘의 지니처럼 제공하고자 하는 소망을 담음
<br>
<br>

&nbsp;&nbsp;&nbsp;&nbsp;__[ 팀원 소개 ]__

|<img src="https://avatars.githubusercontent.com/u/97112125?v=4" width="150" height="150"/>|<img src="https://avatars.githubusercontent.com/u/104715028?v=4" width="150" height="150"/>|<img src="https://avatars.githubusercontent.com/u/172017988?v=4" width="150" height="150"/>|
|:-:|:-:|:-:|
|Kiseong Kim<br/>[@kks1177](https://github.com/kks1177)|ycson<br/>[@syc2618](https://github.com/syc2618)|wldnjs<br/>[@wldnjs9397](https://github.com/wldnjs9397)|
<br>



<!-- --------------------------------------------------------------------------------------------------------------- -->
<!--개발 환경-->
## 개발 환경 (버전참고)
- __Environment__ : STS 4, VS Code(^1.102.0), MySQL Workbench 8.0 CE, git(^2.50.0), github
- __Language__ :  Java 20
- __Framwork__ :
  - Spring Boot
  - SPA (Single Page Application)
  - Spring Web MVC
  - Spring Security
  - Vue
- __Library__
  - Gradle
  - JPA
  - JUnit 4
- __DB__: MySQL DB
- __Communication__ : Notion, Figma, Erdcloud, Canva
<br>



<!-- --------------------------------------------------------------------------------------------------------------- -->
<!--실제 구현 화면/기능-->
## 실제 구현 화면/기능

**[ 로그인 ]**

<사진 추가>
<br>

- 로그인
<br>


**[ 회원가입 ]**

<사진 추가>
<br>

- 회원가입
<br>


**[ AI 문제 생성 ]**

<사진 추가>
<br>

- <설명>
- <설명>
<br>



<!-- --------------------------------------------------------------------------------------------------------------- -->
<!--컨벤션-->
## 컨벤션

- ### 코딩 컨벤션
<img width="763" height="866" alt="image" src="https://github.com/user-attachments/assets/e6e07969-0fc0-43b0-afdf-570adf8ea05f" />

- ### 깃 컨벤션
<img width="736" height="804" alt="image" src="https://github.com/user-attachments/assets/6ff2652c-45ac-44d4-89f6-8f6b603aa35b" />
<br>
<br>



<!-- --------------------------------------------------------------------------------------------------------------- -->
<!--산출물-->
## 산출물

- ### Notion ([코드지니_알라딘 노션 바로가기](https://www.notion.so/27e83a6b63ea8184a6f2d8f6736c70fc?source=copy_link))

- ### 기능 명세서
<img width="1527" height="817" alt="image" src="https://github.com/user-attachments/assets/510247f2-a3be-47fd-be29-2c16510ab010" />

- ### 시스템 아키텍처
<사진 추가>

- ### Wireframe (Figma)
<img width="806" height="614" alt="image" src="https://github.com/user-attachments/assets/3aef1169-e575-4a62-be8a-13379b1e5e44" />

- ### ERD (ERD Cloud)
<img width="1292" height="402" alt="image" src="https://github.com/user-attachments/assets/3feb5ff2-45ca-4850-aab3-0d5b5b5db938" />

---

## 🚀 개발 환경 설정 및 프로젝트 실행 가이드

이 프로젝트는 백엔드(Spring Boot), 프론트엔드(Vue), 그리고 채점 서버(Judge0)로 구성
프로젝트 시작 개발자는 아래 가이드 따라 개발 환경 설정 및 실행 가능

### 1. 필수 준비물

*   **Node.js & npm:** 프론트엔드(Vue) 개발
*   **Java & Gradle:** 백엔드(Spring Boot) 개발

### 2. 초기 설정 (최초 1회)

1.  **Judge0 설정 파일 준비:**
    *   프로젝트 루트에서 `judge0.conf.example` 파일 복사 후 `judge0.conf`로 이름 변경
    *   (judge0.conf.example 파일 그대로 사용 x, 복사해서 이름 변경하기!)
2.  **프론트엔드 의존성 설치:**
    *   `FE` 폴더 이동, `npm install` 실행.

### 3. 프로젝트 실행 (매번)

각 서버 별도 터미널 창 실행 권장

0.  작업 관리자 > 서비스 > MYSQL > 오른쪽 클릭 (시작)
1.  **백엔드 서버 실행:**
    *   `export JAVA_HOME="<JavaJDK 경로>"`
    *   `BE` 폴더에서 `./gradlew bootRun` (또는 IDE에서 실행)
2.  **프론트엔드 서버 실행:**
    *   `FE` 폴더에서 `npm run dev`

### 4. 애플리케이션 접속

*   프론트엔드: `http://localhost:5173`
*   문제 제출 기능 확인: `http://localhost:5173/workbooks/1`
