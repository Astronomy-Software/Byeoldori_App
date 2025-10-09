package com.example.byeoldori.viewmodel

import com.example.byeoldori.R
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Community.FreePost
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.TextFieldValue
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.domain.Community.ReviewComment
import com.example.byeoldori.domain.Content
import com.example.byeoldori.domain.Observatory.Review

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
        createdAt = "20251029",
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-29",
        siteScore = 5,
        equipment = "망원경",
        contentItems = listOf(
            Content.Text("오늘은 태양 흑점을 관측했습니다."),
            Content.Image.Resource(R.drawable.img_dummy),
            Content.Text("날씨가 좋아 관측이 수월했습니다!.")
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
        createdAt = "20251030",
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-29",
        siteScore = 5,
        equipment = "망원경",
        contentItems = listOf(
            Content.Text("오늘은 태양 흑점을 관측했습니다."),
            Content.Image.Resource(R.drawable.img_dummy)
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
        createdAt = "20251129",
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-30",
        siteScore = 5,
        equipment = "망원경",
        contentItems = listOf(
            Content.Text("오늘은 태양 흑점을 관측했습니다."),
            Content.Image.Resource(R.drawable.img_dummy)
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
        createdAt = "20251015",
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-14",
        siteScore = 5,
        equipment = "망원경",
        contentItems = listOf(
            Content.Text("오늘은 태양 흑점을 관측했습니다."),
            Content.Image.Resource(R.drawable.img_dummy)
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
        createdAt = "20251014",
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-10",
        siteScore = 5,
        equipment = "망원경",
        contentItems = listOf(
            Content.Text("오늘은 태양 흑점을 관측했습니다."),
            Content.Image.Resource(R.drawable.img_dummy)
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
        createdAt = "20251027",
        target = "태양",
        site = "충북대학교 천문대",
        date = "2025-10-25",
        siteScore = 5,
        equipment = "망원경",
        contentItems = listOf(
            Content.Text("오늘은 태양 흑점을 관측했습니다."),
            Content.Image.Resource(R.drawable.img_dummy)
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
        createdAt = "20251029",
        profile = R.drawable.profile1,
        contentItems = listOf(
            Content.Text("안녕하세요 오늘 처음 별도리앱 깔았어요"),
            Content.Text("혹시 다들 어디서 관측하시나요??"),
            Content.Image.Resource(R.drawable.img_dummy),
            Content.Text("이런 곳은 어떤가요?? 잘 보이나요?")
        ),
        liked = true
    ),
    FreePost(
        id = "f2",
        title = "처음 뵙겠습니다!2",
        author = "astro2",
        likeCount = 20,
        commentCount = 0,
        viewCount = 87,
        createdAt = "20251030",
        profile = R.drawable.profile1,
        contentItems = listOf(
            Content.Text("오늘 처음 가입했습니다."),
            Content.Image.Resource(R.drawable.img_dummy),
            Content.Text("좋은 관측 장소 공유 부탁드려요!")
        ),
        liked = true
    ),
    FreePost(
        id = "f3",
        title = "처음 뵙겠습니다!3",
        author = "astro3",
        likeCount = 5,
        commentCount = 0,
        viewCount = 100,
        createdAt = "20250929",
        profile = R.drawable.profile1,
        contentItems = listOf(
            Content.Text("안녕하세요 astro3입니다."),
            Content.Text("저는 주로 교외에서 관측해요 🌌")
        ),
        liked = false
    ),
    FreePost(
        id = "f4",
        title = "처음 뵙겠습니다!4",
        author = "astro4",
        likeCount = 1,
        commentCount = 0,
        viewCount = 10,
        createdAt = "20250829",
        profile = R.drawable.profile1,
        contentItems = listOf(
            Content.Text("astro4 입니다. 잘 부탁드려요!"),
            Content.Image.Resource(R.drawable.img_dummy)
        ),
        liked = false
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
        createdAt = "20251015",
        contentItems = listOf(
            Content.Text("망원경 종류와 기본 조작을 배웁니다."),
            Content.Image.Resource(R.drawable.img_dummy),
            Content.Text("실습 포함: 파인더 정렬, 배율 계산")
        ),
        liked = true,
        target = "망원경",
        averageScore = 4.5
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
        createdAt = "20251007",
        contentItems = listOf(
            Content.Text("유성우 예보 읽는 법과 관측 팁을 다룹니다."),
            Content.Image.Resource(R.drawable.img_dummy),
            Content.Text("필수 장비 체크리스트 제공")
        ),
        liked = false,
        target = "유성우",
        averageScore = 4.8
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
        createdAt = "20251020",
        contentItems = listOf(
            Content.Text("광해가 심한 환경에서의 관측 전략."),
            Content.Text("필터 사용과 관측 대상 추천")
        ),
        liked = false,
        target = "별",
        averageScore = 3.5
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
