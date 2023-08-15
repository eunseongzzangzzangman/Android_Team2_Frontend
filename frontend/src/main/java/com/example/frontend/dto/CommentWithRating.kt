package com.example.frontend.dto

data class CommentWithRating(
    val comment: String,
    val rating: Int,
    val time: Long,
    val uid: String,
    val message : String = ""
)
