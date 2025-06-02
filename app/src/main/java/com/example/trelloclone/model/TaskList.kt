package com.example.trelloclone.model

data class TaskList(
    val id: String = "",
    val boardId: String = "",
    val name: String = "",
    val position: Int = 0,
    val createdBy: String = "",
    val createdDate: Long = System.currentTimeMillis()
)