package com.example.trelloclone.model

data class BoardMember(
    val userId: String = "",
    val boardId: String = "",
    val role: String = "member" // owner, admin, member
)