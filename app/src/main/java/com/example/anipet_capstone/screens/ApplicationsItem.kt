package com.example.anipet_capstone.screens

data class ApplicationItem(
    val id: String,
    val pet_id: String,
    val user_id: String,
    val applicant_name: String,
    val message: String,
    val status: String,
    val qr_code: String?,
    val created_at: String,
    val pet_name: String? = null,
    val breed: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val id_documents: List<String>? = null,
    val house_photos: List<String>? = null,
    val interview_datetime: String? = null,
    val admin_notes: String? = null,
    val return_request_status: String? = null,
    val return_request_reason: String? = null,
    val penalty_amount: Double? = null,
    val penalty_paid: Int? = null
)