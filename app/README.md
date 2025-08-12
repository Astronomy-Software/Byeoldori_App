# Android Drawable 네이밍 컨벤션 (PNG, XML 전용)

본 문서는 Jetpack Compose 및 Android 프로젝트에서 **PNG, XML, JPG** 이미지 리소스 네이밍 규칙을 정리한 가이드입니다.  
UI 일관성 유지와 협업 효율성을 위해 반드시 아래 규칙을 준수해주세요.
혹시라도 SVG를 사용해야한다면 assets 폴더안에 넣어서 사용해주세요. 하지만 아이콘은 XML로 사용하는게 좋다고 합니다..

---

## 1. 기본 규칙 (Android 공식 가이드 기반)
> 출처: [Android Developers - App resources](https://developer.android.com/guide/topics/resources/providing-resources)

- 파일명은 **소문자(a–z)**, 숫자(0–9), 언더스코어(`_`)만 사용
- 공백, 대문자, 하이픈(`-`) 사용 금지
- 의미 있는 이름 작성 (역할, 상태, 크기, 색상 등을 명확히)
- 동일한 역할/종류의 파일은 **접두사(Prefix)**로 그룹화
---

## 2. 접두사(Prefix) 규칙
> 참고: [XToolkit.WhiteLabel Resource Naming](https://softeq.github.io/XToolkit.WhiteLabel/articles/practices/android-res-naming.html),  
> [Medium - Best practices for happy Android resources](https://medium.com/@veniosg/best-practices-for-happy-android-resources-9445c1b521d6)

| Prefix | 설명 | 예시 |
|--------|------|------|
| `ic_`  | 아이콘 (Icon) | `ic_user.png`, `ic_lock_filled.svg` |
| `bg_`  | 배경 이미지 | `bg_login.png`, `bg_gradient.svg` |
| `btn_` | 버튼 그래픽 | `btn_login_normal.png`, `btn_login_pressed.png` |
| `img_` | 일반 이미지/일러스트 | `img_welcome_illustration.jpg` |
| `logo_`| 로고 | `logo_app.svg`, `logo_partner.png` |
| `banner_` | 배너 이미지 | `banner_event.png` |
| `divider_` | 구분선/라인 이미지 | `divider_horizontal.png` |

---

## 3. 상태(Suffix) 규칙
> 참고: [Medium - A designer's guide for naming Android assets](https://medium.com/@AkhilDad/a-designers-guide-for-naming-android-assets-f790359d11e5)

- UI 상태별 그래픽은 **Suffix**로 구분
- 주요 상태:
    - `_normal` → 기본 상태
    - `_pressed` → 클릭/터치 상태
    - `_disabled` → 비활성화 상태
    - `_selected` → 선택됨 상태
    - `_focused` → 포커스 상태

**예시**
btn_login_normal.png
btn_login_pressed.png
ic_checkbox_selected.svg
---

## 4. 크기/색상 규칙 (선택)
- 필요 시 **크기**(`24dp`, `48dp`) 또는 **색상**(`black`, `white`, `primary`)을 suffix로 표기
- 예시:

---
ic_arrow_back_white_24dp.svg
ic_arrow_back_black_48dp.png
## 5. 예시 모음

---
ic_user.png
ic_user_selected.svg
bg_login.png
btn_login_normal.png
btn_login_pressed.png
img_welcome_illustration.jpg
logo_app.svg
banner_event.png
divider_horizontal.png
## 6. 참고 자료
- [Android Developers - Providing Resources](https://developer.android.com/guide/topics/resources/providing-resources)
- [XToolkit.WhiteLabel Resource Naming](https://softeq.github.io/XToolkit.WhiteLabel/articles/practices/android-res-naming.html)
- [Medium - Best practices for happy Android resources](https://medium.com/@veniosg/best-practices-for-happy-android-resources-9445c1b521d6)
- [Medium - A designer's guide for naming Android assets](https://medium.com/@AkhilDad/a-designers-guide-for-naming-android-assets-f790359d11e5)
- [StackOverflow - Drawable files name convention](https://stackoverflow.com/questions/49805803/drawable-files-name-convention)
