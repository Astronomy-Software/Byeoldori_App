package com.example.byeoldori.viewmodel.Observatory

import com.example.byeoldori.R
import com.example.byeoldori.viewmodel.Community.EduProgram
import com.example.byeoldori.viewmodel.Community.FreePost
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.TextFieldValue
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.viewmodel.Community.ReviewComment
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
        commentCount = 10,
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
        commentCount = 10,
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
        commentCount = 10,
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
        commentCount = 10,
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
        commentCount = 10,
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
        commentCount = 10,
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
        likeCount = 3, commentCount = 1, createdAt = 202510291750
    ),
    ReviewComment(
        id = "c2", reviewId = "r1",
        author = "별헤는 곰돌이", profile = R.drawable.profile1,
        content = "충북대 대운동장 좋아요!",
        likeCount = 1, commentCount = 0, createdAt = 202510291755
    ),
    ReviewComment(
        id = "c3", reviewId = "r2",
        author = "astro_21", profile = R.drawable.profile1,
        content = "오늘 투명도 좋았습니다 🙌",
        likeCount = 2, commentCount = 0, createdAt = 202510291820
    ),
    ReviewComment(
        id = "c4", reviewId = "r2",
        author = "skylover", profile = R.drawable.profile1,
        content = "광해만 조금만 덜하면 최고!",
        likeCount = 0, commentCount = 0, createdAt = 202510291825
    ),
    ReviewComment(
        id = "c5", reviewId = "r3",
        author = "아이마카", profile = R.drawable.profile1,
        content = "내일도 관측 예정이에요",
        likeCount = 5, commentCount = 2, createdAt = 202510281930
    ),
    ReviewComment(
        id = "c6", reviewId = "r4",
        author = "meteor", profile = R.drawable.profile1,
        content = "유성 두 개 봤습니다!",
        likeCount = 4, commentCount = 1, createdAt = 202510271145
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
        createdAt = 202510251600
    ),
    ReviewComment(
        id = "fc2", reviewId = "f1",
        author = "astro_friend",
        profile = R.drawable.profile1,
        content = "오산천도 괜찮아요. 접근성이 좋아요!",
        likeCount = 1, commentCount = 0,
        createdAt = 202510251630
    ),
    ReviewComment(
        id = "fc3", reviewId = "f2",
        author = "별헤는 밤",
        profile = R.drawable.profile1,
        content = "저는 충북대 대운동장에서 자주 봅니다.",
        likeCount = 0, commentCount = 0,
        createdAt = 202510291510
    ),
    ReviewComment(
        id = "fc4", reviewId = "f3",
        author = "meteor_chaser",
        profile = R.drawable.profile1,
        content = "요즘 투명도가 좋아서 별이 잘 보여요!",
        likeCount = 3, commentCount = 1,
        createdAt = 202510301600
    ),
    ReviewComment(
        id = "fc5", reviewId = "f4",
        author = "astro4",
        profile = R.drawable.profile1,
        content = "저도 같은 생각이에요. 좋은 장소 공유해요~",
        likeCount = 1, commentCount = 0,
        createdAt = 202510281530
    )
)



//교육 프로그램 더미 데이터
val dummyPrograms = listOf(
    EduProgram("1", "유성우 관측 방법1", "아이마카1", 5.0f, 70, 10, R.drawable.img_dummy,60,202510290000),
    EduProgram("2", "유성우 관측 방법2", "아이마카2", 5.0f, 40, 10, R.drawable.img_dummy,50,202510290100),
    EduProgram("3", "유성우 관측 방법3", "아이마카3", 5.0f, 30, 10, R.drawable.img_dummy,40,202510290500),
    EduProgram("4", "유성우 관측 방법4", "아이마카4", 5.0f, 65, 10, R.drawable.img_dummy,30,202510290700),
    EduProgram("5", "유성우 관측 방법5", "아이마카5", 5.0f, 55, 10, R.drawable.img_dummy,20,202510291600),
    EduProgram("6", "유성우 관측 방법6", "아이마카6", 5.0f, 50, 10, R.drawable.img_dummy,100,202510291900),
    EduProgram("7", "유성우 관측 방법7", "아이마카7", 5.0f, 100, 10, R.drawable.img_dummy,90,202510291400),
    EduProgram("8", "유성우 관측 방법8", "아이마카8", 5.0f, 205, 10, R.drawable.img_dummy,70,202510291500)
)

// === 자유게시판 더미 데이터 ===
val dummyFreePosts = mutableStateListOf(
    FreePost(
        id = "f1",
        title = "처음 뵙겠습니다!1",
        author = "astro1",
        likeCount = 12,
        commentCount = 10,
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
        commentCount = 10,
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
        commentCount = 10,
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
        commentCount = 10,
        viewCount = 10,
        createdAt = 202510281500,
        profile = R.drawable.profile1,
        contentItems = listOf(
            EditorItem.Paragraph(value = TextFieldValue("astro4 입니다. 잘 부탁드려요!")),
            EditorItem.Photo(model = R.drawable.img_dummy)
        )
    )
)
