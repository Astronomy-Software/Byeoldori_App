package com.example.byeoldori.domain.Community

enum class CommentSourceType { REVIEW, FREE, EDUCATION }

data class ReviewComment(
    val id: Long,
    val reviewId: Long, //어느 댓글에 달렸는지
    val parentId: Long?, //대댓글인 경우 부모 댓글의 id
    val authorId: Long,
    val authorNickname: String?,
    val authorProfileImageUrl: String? = null,
    val content: String? = null,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val deleted: Boolean = false,
    val liked: Boolean
)

data class MyCommentUi(
    val source: CommentSourceType,
    val postId: Long,
    val postTitle: String,
    val postAuthorName: String,
    val commentId: Long,
    val content: String,
    val createdAt: String
)

data class MyCommentGroup(
    val source: CommentSourceType,
    val postId: Long,
    val postTitle: String,
    val postAuthorName: String,
    val postCreatedAt: String,
    val postAuthorProfileUrl: String?,
    val myComments: List<MyCommentUi>
)