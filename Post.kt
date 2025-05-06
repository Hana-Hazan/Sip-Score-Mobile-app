package com.example.sipscore.models

data class Post(
    val id: String,
    val image: String,
    val email: String,
    val username: String,
    val imageProfile: String,
    var content: String,
    val date: String,
    val likes: List<String> = emptyList()
)
