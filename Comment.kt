package com.example.sipscore.models

import android.R

data class Comment(
    val commentId: String = "",
    val commenterUsername: String = "",
    val commenterProfileImage: String = "",
    val content: String = "",
    val date: String = "",
    var likes: List<String> = emptyList(),
    var dislikes: List<String> = emptyList()
)
