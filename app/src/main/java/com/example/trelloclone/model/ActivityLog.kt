package com.example.trelloclone.model

data class ActivityLog(
    val id: String = "",
    val userId: String = "",
    val action: String = "",       // ví dụ: added card, moved card, etc.
    val targetId: String = "",     // id đối tượng (card, board...)
    val targetType: String = "",   // "card", "board", etc.
    val createdDate: Long = System.currentTimeMillis()
)