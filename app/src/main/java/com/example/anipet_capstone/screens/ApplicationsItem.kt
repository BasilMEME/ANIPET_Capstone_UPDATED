package com.example.anipet_capstone.screens

data class ApplicationItem(
    val id: String,
    val pet_id: String,
    val user_id: String,
    val applicant_name: String,
    val message: String,
    val status: String,
    val qr_code: String?,
    val created_at: String
)