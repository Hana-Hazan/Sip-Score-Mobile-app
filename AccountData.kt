package com.example.sipscore.models

data class AccountData(
    val email: String="",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val password: String = "",
    val ratings: MutableMap<String, Double> = mutableMapOf(),
    val profileImage: String? = null,
    val favorites: List<String> = listOf()
)
