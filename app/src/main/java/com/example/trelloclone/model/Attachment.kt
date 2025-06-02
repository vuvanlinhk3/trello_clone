package com.example.trelloclone.model

data class Attachment(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val uploadedBy: String = "",
    val uploadedDate: Long = System.currentTimeMillis()
)