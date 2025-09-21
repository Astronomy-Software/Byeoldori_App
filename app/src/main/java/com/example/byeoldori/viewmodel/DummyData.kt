package com.example.byeoldori.viewmodel

import com.example.byeoldori.R
import com.example.byeoldori.viewmodel.Community.EduProgram
import com.example.byeoldori.viewmodel.Community.FreePost
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.TextFieldValue
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.viewmodel.Community.ReviewComment
import com.example.byeoldori.viewmodel.Observatory.CurrentWeather
import com.example.byeoldori.viewmodel.Observatory.DailyForecast
import com.example.byeoldori.viewmodel.Observatory.HourlyForecast
import com.example.byeoldori.viewmodel.Observatory.Observatory
import com.example.byeoldori.viewmodel.Observatory.ObservatoryType
import com.example.byeoldori.viewmodel.Observatory.Review
import com.naver.maps.geometry.LatLng

val observatoryList = listOf(
    Observatory(
        name = "오산천",
        type = ObservatoryType.POPULAR,
        latLng = LatLng(37.1570906, 127.0703307),
        reviewCount = 103,
        likeCount = 57,
        avgRating = 4.3f,
        address = "경기도 오산시 오산천로 254-5",
        imageRes = R.drawable.img_dummy,
        suitability = 87
    ),
    Observatory(
        name = "필봉산",
        type = ObservatoryType.GENERAL,
        latLng = LatLng(37.179097404593584, 127.07212073869198),
        reviewCount = 150,
        likeCount = 99,
        avgRating = 4.2f,
        address = "경기도 오산시 내삼미동 산 21-1",
        imageRes = R.drawable.img_dummy,
        suitability = 85
    ),
    Observatory(
        name = "배티공원",
        type = ObservatoryType.GENERAL,
        latLng = LatLng(36.6266086351, 127.4653234453252),
        reviewCount = 69,
        likeCount = 12,
        avgRating = 3.9f,
        address = "충청북도 청주시 서원구 개신동 3-16",
        imageRes = R.drawable.img_dummy,
        suitability = 88
    ),
    Observatory(
        name = "구룡산",
        type = ObservatoryType.POPULAR,
        latLng = LatLng(36.61834002153799, 127.46435709201829),
        reviewCount = 79,
        likeCount = 150,
        avgRating = 4.8f,
        address = "충북 청주시 상당구 문의면 덕유리",
        imageRes = R.drawable.img_dummy,
        suitability = 92
    ),
)

//현재 날씨 더미
val dummyCurrentWeather = CurrentWeather(
    temperature = "14°",
    humidity = "35%",
    windSpeed = "→ 3m/s",
    suitability = "75%"
)

//날씨 (시간별) 더미 데이터
val dummyHourlyForecasts = listOf(
    HourlyForecast("5.23", "4시", "15°", "cloud_sun", "60%", "85%"),
    HourlyForecast("5.23", "5시", "16°", "sunny",     "55%", "82%"),
    HourlyForecast("5.23", "6시", "17°", "rain",      "70%", "60%"),
    HourlyForecast("5.24", "1시", "13°", "cloud_moon","80%", "90%"),
    HourlyForecast("5.24", "2시", "12°", "cloud_sun", "85%", "88%"),
    HourlyForecast("5.24", "3시", "14°", "sunny",     "60%", "60%")
    // ...
)

//날씨 (일별) 더미 데이터
val dummyDailyForecasts = listOf(
    DailyForecast("5.27", "100%", "sunny",      "cloud",      "27°", "13°", "85%"),
    DailyForecast("5.28", "80%",  "cloud",      "rain",       "25°", "12°", "60%"),
    DailyForecast("5.29", "90%",  "rain",       "rain",       "23°", "11°", "45%"),
    DailyForecast("5.30", "100%", "rain",       "rain",       "22°", "10°", "20%"),
    DailyForecast("5.31", "100%", "cloud_sun",  "rain",       "23°", "9°",  "40%"),
    DailyForecast("6.1",  "100%", "rain",       "cloud_moon", "22°", "11°", "35%")
    // ...
)

//리뷰 더미 데이터
val dummyReviews = mutableStateListOf(
    Review(
        id = "r1",
        title = "태양 흑점 본 날1",
        author = "아이마카1",
        rating = 5,
        likeCount = 70,
        commentCount = 0,
        profile = R.drawable.profile1,
        viewCount = 60,
        createdAt = 202510290000,
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-29",
        siteScore = 5,
        equipment = "망원경",
        startTime = "22:00",
        endTime = "23:30",
        // ✅ 본문 + 이미지
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("오늘은 태양 흑점을 관측했습니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy),
            EditorItem.Paragraph(value = TextFieldValue("날씨가 좋아 관측이 수월했습니다!."))
        )
    ),
    Review(
        id = "r2",
        title = "태양 흑점 본 날2",
        author = "아이마카2",
        rating = 5,
        likeCount = 80,
        commentCount = 0,
        profile = R.drawable.profile1,
        viewCount = 100,
        createdAt = 202510290000,
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-29",
        siteScore = 5,
        equipment = "망원경",
        startTime = "12:00",
        endTime = "16:30",
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("오늘은 태양 흑점을 관측했습니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy)
        )
    ),
    Review(
        id = "r3",
        title = "태양 흑점 본 날3",
        author = "아이마카3",
        rating = 5,
        likeCount = 40,
        commentCount = 0,
        profile = R.drawable.profile1,
        viewCount = 80,
        createdAt = 202510280000,
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-30",
        siteScore = 5,
        equipment = "망원경",
        startTime = "22:00",
        endTime = "23:30",
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("오늘은 태양 흑점을 관측했습니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy)
        )
    ),
    Review(
        id = "r4",
        title = "태양 흑점 본 날4",
        author = "아이마카4",
        rating = 5,
        likeCount = 30,
        commentCount = 0,
        profile = R.drawable.profile1,
        viewCount = 60,
        createdAt = 202510270000,
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-14",
        siteScore = 5,
        equipment = "망원경",
        startTime = "22:00",
        endTime = "23:30",
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("오늘은 태양 흑점을 관측했습니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy)
        )
    ),
    Review(
        id = "r5",
        title = "태양 흑점 본 날5",
        author = "아이마카5",
        rating = 5,
        likeCount = 20,
        commentCount = 0,
        profile = R.drawable.profile1,
        viewCount = 20,
        createdAt = 202510260000,
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-10",
        siteScore = 5,
        equipment = "망원경",
        startTime = "22:00",
        endTime = "23:30",
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("오늘은 태양 흑점을 관측했습니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy)
        )
    ),
    Review(
        id = "r6",
        title = "태양 흑점 본 날6",
        author = "아이마카6",
        rating = 5,
        likeCount = 5,
        commentCount = 0,
        profile = R.drawable.profile1,
        viewCount = 5,
        createdAt = 202510250000,
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-25",
        siteScore = 5,
        equipment = "망원경",
        startTime = "22:00",
        endTime = "23:30",
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("오늘은 태양 흑점을 관측했습니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy)
        )
    ),
)

//댓글 더미 데이터(관측 리뷰)
val dummyReviewComments = mutableStateListOf(
    ReviewComment(
        id = "c1", reviewId = "r1",
        author = "아이마카", profile = R.drawable.profile1,
        content = "색다른 곳 있으면 알려주세요~",
        likeCount = 3, commentCount = 1, createdAt = 202510291750,
        parentId = null
    ),
    ReviewComment(
        id = "c2", reviewId = "r1",
        author = "별헤는 곰돌이", profile = R.drawable.profile1,
        content = "충북대 대운동장 좋아요!",
        likeCount = 1, commentCount = 0, createdAt = 202510291755,
        parentId = null
    ),
    ReviewComment(
        id = "c3", reviewId = "r2",
        author = "astro_21", profile = R.drawable.profile1,
        content = "오늘 투명도 좋았습니다 🙌",
        likeCount = 2, commentCount = 0, createdAt = 202510291820,
        parentId = null
    ),
    ReviewComment(
        id = "c4", reviewId = "r2",
        author = "skylover", profile = R.drawable.profile1,
        content = "광해만 조금만 덜하면 최고!",
        likeCount = 0, commentCount = 0, createdAt = 202510291825,
        parentId = null
    ),
    ReviewComment(
        id = "c5", reviewId = "r3",
        author = "아이마카", profile = R.drawable.profile1,
        content = "내일도 관측 예정이에요",
        likeCount = 5, commentCount = 2, createdAt = 202510281930,
        parentId = null
    ),
    ReviewComment(
        id = "c6", reviewId = "r4",
        author = "meteor", profile = R.drawable.profile1,
        content = "유성 두 개 봤습니다!",
        likeCount = 4, commentCount = 1, createdAt = 202510271145,
        parentId = null
    )
)

// 댓글 더미 데이터(자유게시판)
val dummyFreeComments = mutableStateListOf(
    ReviewComment(
        id = "fc1", reviewId = "f1",
        author = "star_gazer",
        profile = R.drawable.profile1,
        content = "환영합니다! 저는 주로 교외에서 관측해요 🌌",
        likeCount = 2, commentCount = 0,
        createdAt = 202510251600,
        parentId = null
    ),
    ReviewComment(
        id = "fc2", reviewId = "f1",
        author = "astro_friend",
        profile = R.drawable.profile1,
        content = "오산천도 괜찮아요. 접근성이 좋아요!",
        likeCount = 1, commentCount = 0,
        createdAt = 202510251630,
        parentId = null
    ),
    ReviewComment(
        id = "fc3", reviewId = "f2",
        author = "별헤는 밤",
        profile = R.drawable.profile1,
        content = "저는 충북대 대운동장에서 자주 봅니다.",
        likeCount = 0, commentCount = 0,
        createdAt = 202510291510,
        parentId = null
    ),
    ReviewComment(
        id = "fc4", reviewId = "f3",
        author = "meteor_chaser",
        profile = R.drawable.profile1,
        content = "요즘 투명도가 좋아서 별이 잘 보여요!",
        likeCount = 3, commentCount = 1,
        createdAt = 202510301600,
        parentId = null
    ),
    ReviewComment(
        id = "fc5", reviewId = "f4",
        author = "astro4",
        profile = R.drawable.profile1,
        content = "저도 같은 생각이에요. 좋은 장소 공유해요~",
        likeCount = 1, commentCount = 0,
        createdAt = 202510281530,
        parentId = null
    )
)

// === 자유게시판 더미 데이터 ===
val dummyFreePosts = mutableStateListOf(
    FreePost(
        id = "f1",
        title = "처음 뵙겠습니다!1",
        author = "astro1",
        likeCount = 12,
        commentCount = 0,
        viewCount = 120,
        createdAt = 202510251500,
        profile = R.drawable.profile1,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("안녕하세요 오늘 처음 별도리앱 깔았어요")),
            EditorItem.Paragraph(value = TextFieldValue("혹시 다들 어디서 관측하시나요??")),
            EditorItem.Photo(model = R.drawable.img_dummy),
            EditorItem.Paragraph(value = TextFieldValue("이런 곳은 어떤가요?? 잘 보이나요?"))
        )
    ),
    FreePost(
        id = "f2",
        title = "처음 뵙겠습니다!2",
        author = "astro2",
        likeCount = 20,
        commentCount = 0,
        viewCount = 87,
        createdAt = 202510291500,
        profile = R.drawable.profile1,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("오늘 처음 가입했습니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy),
            EditorItem.Paragraph(value = TextFieldValue("좋은 관측 장소 공유 부탁드려요!"))
        )
    ),
    FreePost(
        id = "f3",
        title = "처음 뵙겠습니다!3",
        author = "astro3",
        likeCount = 5,
        commentCount = 0,
        viewCount = 100,
        createdAt = 202510301500,
        profile = R.drawable.profile1,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("안녕하세요 astro3입니다.")),
            EditorItem.Paragraph(value = TextFieldValue("저는 주로 교외에서 관측해요 🌌"))
        )
    ),
    FreePost(
        id = "f4",
        title = "처음 뵙겠습니다!4",
        author = "astro4",
        likeCount = 1,
        commentCount = 0,
        viewCount = 10,
        createdAt = 202510281500,
        profile = R.drawable.profile1,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("astro4 입니다. 잘 부탁드려요!")),
            EditorItem.Photo(model = R.drawable.img_dummy)
        )
    )
)

//교육 프로그램 더미 데이터
val dummyPrograms = mutableStateListOf(
    EduProgram(
        id = "p1",
        title = "초보자를 위한 망원경 기초",
        author = "아이마카",
        profile = R.drawable.profile1,
        rating = 4.5f,
        likeCount = 32,
        commentCount = 0,
        viewCount = 120,
        createdAt = 202510290900,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("망원경 종류와 기본 조작을 배웁니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy),
            EditorItem.Paragraph(value = TextFieldValue("실습 포함: 파인더 정렬, 배율 계산"))
        )
    ),
    EduProgram(
        id = "p2",
        title = "유성우 관측 A to Z",
        author = "astro_mentor",
        profile = R.drawable.profile1,
        rating = 4.8f,
        likeCount = 57,
        commentCount = 0,
        viewCount = 210,
        createdAt = 202510291100,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("유성우 예보 읽는 법과 관측 팁을 다룹니다.")),
            EditorItem.Photo(model = R.drawable.img_dummy),
            EditorItem.Paragraph(value = TextFieldValue("필수 장비 체크리스트 제공"))
        )
    ),
    EduProgram(
        id = "p3",
        title = "도심에서 별보기",
        author = "night_sky",
        profile = R.drawable.profile1,
        rating = 4.2f,
        likeCount = 18,
        commentCount = 0,
        viewCount = 95,
        createdAt = 202510291430,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("광해가 심한 환경에서의 관측 전략.")),
            EditorItem.Paragraph(value = TextFieldValue("필터 사용과 관측 대상 추천"))
        )
    )
)

// 교육 프로그램 댓글 더미
// (ReviewComment 재사용)
val dummyProgramComments = mutableStateListOf(
    ReviewComment(
        id = "pc1", reviewId = "p1",
        author = "star_gazer", profile = R.drawable.profile1,
        content = "실습 파트가 특히 유용했어요!",
        likeCount = 3, commentCount = 1, createdAt = 202510291230,
        parentId = null
    ),
    ReviewComment(
        id = "pc2", reviewId = "p1",
        author = "meteor_chaser", profile = R.drawable.profile1,
        content = "다음 기수는 언제 열리나요?",
        likeCount = 1, commentCount = 0, createdAt = 202510291245,
        parentId = null
    ),
    ReviewComment(
        id = "pc3", reviewId = "p2",
        author = "astro_newbie", profile = R.drawable.profile1,
        content = "체크리스트 덕분에 첫 관측 잘했습니다 🙌",
        likeCount = 5, commentCount = 0, createdAt = 202510291500,
        parentId = null
    )
)
