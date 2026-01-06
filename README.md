# 충북 대학교 소프트웨어학과 2025년도 졸업 작품 "별도리"
- Team 천문소프트 
- 학생 및 천체 관측 초보자 들을 위한 천체 관측 교육용 Application 입니다.
- 구성 인원 : 서범수 , 윤태영 , 김채영

## App 구동 방법
1. Git pull 받은후 Android Studio 연결
2. Android studio -> File -> Sync project with Gradle file 실행
3. 이후 앱 실행

## 개발 환경
- Java 21 
- Kotlin 1.9.23
- JetpackCompose 1.5.12

### 환경 설정 방법
- local.properties 파일에 아래의 내용 작성
  - NAVER_CLIENT_ID=Your Client ID
  - NAVER_CLIENT_SECRET=Your Client Secret Key


---

# 🌌 프로젝트 소개 (별도리)
'초보자 및 학생을 위한 **천체 관측 교육용 Android 애플리케이션'**  
**'초보자 및 학생들을 위한 천체 관측 교육 애플리케이션'** 은 사용자가 체계적이고 신뢰할 수 있는 천문학 정보를 쉽게 접할 수 있도록 설계되었으며, 마스코트 캐릭터가 진행하는 학습과 관측을 결합한 흥미로운 프로그램을 통해 천체 관측 경험이 없는 사용자들이 관측에 대한 두려움을 극복하고 천문학에 쉽게 입문할 수 있도록 돕는 것이 주요 목적입니다.



## ✨ 주요 기능 소개
### 1. 관측 교육 프로그램
- 천체 관측 및 교육에 관하여 천체 별로 관리하여 사용자의 관측 및 교육에 도움을 줍니다.
- 마스코트 캐릭터를 사용해 천체에 대한 학습을 지원해주고, 천체 관측을 단계별로 안내합니다.


### 2. 마스코트 캐릭터
- 천체 관측 학습을 지원해주는 역할을 합니다.
- 말풍선을 이용한 대화 방식과 여러 감정 표현을 통하여 사용자와 상호작용을 할 수 있습니다.

### 😊 마스코트 감정 표현들
관측 가이드의 진행 상황과 사용자 행동에 따라 마스코트 캐릭터의 감정 상태를 시각적으로 표현합니다.
| Crying (우는 상태) | Standing (기본/대기 상태) | Her (놀란 상태) |
|-------------------|---------------------------|----------------|
| <img src="https://github.com/user-attachments/assets/729d85eb-5361-4c2b-98e5-f595f94b2609" width="220"/> | <img src="https://github.com/user-attachments/assets/59f2a952-fced-48df-88d0-9a0d5b079954" width="220"/> | <img src="https://github.com/user-attachments/assets/783125e9-1194-41b5-8085-d756ccb32628" width="220"/> |

| Angry (화난 상태) | Happy (즐거운 상태) |  
|------------------|--------------------|
| <img src="https://github.com/user-attachments/assets/3f023764-3f59-42f5-ab82-6e739c3e61ad" width="220"/> | <img src="https://github.com/user-attachments/assets/cf82ccc8-a2cb-44cd-96f5-6aa67e96e0d6" width="220"/> |  


### 3. 별지도 기능
- 자기장 센서, 자이로스코프 기능, GPS 시스템을 이용하여 실시간으로 별자리와 천체의 위치를 시각적으로 표시해 줍니다.

### ⭐ 별지도 페이지(ex.Deneb) 
별자리와 주요 천체를 지도 형태로 제공하며, 검색을 통해 특정 천체의 위치와 상세 정보를 확인할 수 있습니다.

| 별지도 | 별지도 (데네브 검색) | 별지도 (데네브 상세) | 별지도 (데네브 상세) |
|-------|--------------------|---------------------|--------------------------|
| <img src="https://github.com/user-attachments/assets/4b313549-2935-4d97-aa2d-f71e1db3e5b6" width="180"/> | <img src="https://github.com/user-attachments/assets/3b0a2401-ae80-45d7-9928-d587f8e8b81e" width="180"/> | <img src="https://github.com/user-attachments/assets/c903442b-9c61-410e-a28d-761e957e455c" width="180"/> | <img src="https://github.com/user-attachments/assets/a983f529-5d76-48fa-8c93-5984a704217b" width="180"/> |


### 4. 관측지 정보 제공
- 사용자가 천체 관측 일정을 계획할 때, 날씨, 달의 위상, 광량 등을 종합적으로 고려해서 관측 적합도를 사용자에게 제공해주어 관측에 적합한 시간이나 장소를 정할 수 있도록 도움을 줍니다.

### 📍 관측지 페이지
지도 기반으로 전국 주요 관측지를 제공하며, 관측 환경과 현재 조건을 종합적으로 확인할 수 있습니다.

광공해 지도 오버레이를 통한 관측 환경을 확인할 수 있습니다.

| 관측지 지도 | 광공해 오버레이 | 관측지 선택 |
|------------|---------------|------------|
| <img src="https://github.com/user-attachments/assets/1a7aef7f-f837-4825-ac06-45f3cb632474" width="180"/> | <img src="https://github.com/user-attachments/assets/d7828556-4cf5-4977-9397-78da508a7af3" width="180"/> | <img src="https://github.com/user-attachments/assets/344eed25-2e96-4308-a6fc-5bc9361852dc" width="180"/> |

| 관측지 상세 정보(날씨) | 관측지 상세 정보(관측 후기) |  
|----------------|-----------------|
| <img src="https://github.com/user-attachments/assets/2b2db648-824e-4b5c-be0c-a24515bab932" width="220"/> | <img src="https://github.com/user-attachments/assets/50c56124-340c-493f-bd3a-77fe20075105" width="220"/> |  



### 5. 관측 커뮤니티
- 사용자가 직접 관측한 결과를 커뮤니티에 게시하거나 이를 공유할 수 있습니다.
- 다른 사용자들이 작성한 게시글에 대해 소통할 수 있고 댓글을 통해 정보를 공유할 수 있습니다.

### 6. 마이페이지
- 회원가입, 로그인, 사용자 정보 수정 및 탈퇴 등 사용자의 계정 관리 기능과 개인의 관측 계획 및 기록을 캘린더에 저장하여 관측 일정을 관리할 수 있습니다. 










