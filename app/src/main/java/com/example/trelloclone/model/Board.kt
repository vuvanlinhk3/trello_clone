package com.example.trelloclone.model

data class Board(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val image: String? = null,
    val createdBy: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val members: List<String> = listOf(),
    val isPrivate: Boolean = true,
    val labels: List<Label> = listOf(),
    val permissionSettings: Map<String, String> = mapOf()  // uid -> role
)