package com.example.trelloclone.model

data class Card(
    val id: String = "",
    val listId: String = "",
    val title: String = "",
    val description: String? = null,
    val assignedTo: List<String> = listOf(),
    val dueDate: Long? = null,
    val labels: List<String> = listOf(),
    val comments: List<Comment> = listOf(),
    val attachments: List<Attachment> = listOf(),
    val checklists: List<Checklist> = listOf(),
    val createdBy: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val position: Int = 0
)