package com.example.trelloclone.model

data class Checklist(
    val id: String = "",
    val title: String = "",
    val items: List<ChecklistItem> = listOf()
)

data class ChecklistItem(
    val id: String = "",
    val text: String = "",
    val isChecked: Boolean = false
)