package com.example.anipet_capstone.models

data class ApplicationStatusResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ApplicationTrackingData? = null,
    val progress: ApplicationProgress? = null
)

data class ApplicationTrackingData(
    val id: Int,
    val pet_id: Int,
    val user_id: Int,
    val applicant_name: String,
    val message: String? = null,
    val status: String,
    val qr_code: String? = null,
    val admin_notes: String? = null,
    val interview_datetime: String? = null,
    val created_at: String,
    val screened_by: Int? = null,
    val pet_name: String,
    val pet_image: String? = null,
    val user_name: String,
    val user_email: String
)

data class ApplicationProgress(
    val current_status: String,
    val completed_steps: Int,
    val total_steps: Int,
    val statuses: List<String>
)
