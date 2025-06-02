package com.example.trelloclone.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val createdDate: Long = System.currentTimeMillis()
)