package com.example.anipet_capstone.models

data class VerifiedApplication(
    val id: String,
    val user_id: String,
    val pet_id: String,
    val applicant_name: String,
    val message: String,
    val status: String,
    val qr_code: String,
    val created_at: String,
    val pet_name: String,
    val breed: String,
    val age: String,
    val gender: String
)