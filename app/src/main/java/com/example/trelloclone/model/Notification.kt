package com.example.trelloclone.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: String = "",           // comment, assign, dueDate
    val message: String = "",
    val isRead: Boolean = false,
    val createdDate: Long = System.currentTimeMillis()
)