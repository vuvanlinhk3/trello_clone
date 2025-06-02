package com.example.trelloclone.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String? = null,
    val bio: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val boardsOwned: List<String> = listOf(),
    val boardsJoined: List<String> = listOf()
)