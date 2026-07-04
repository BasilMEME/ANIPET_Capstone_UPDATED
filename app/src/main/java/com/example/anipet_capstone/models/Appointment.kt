package com.example.anipet_capstone.models

data class Appointment(
    val id: String,
    val user_id: String,
    val pet_id: String? = null,
    val title: String,
    val details: String,
    val scheduled_at: String? = null,
    val status: String,
    val created_at: String,
    val application_id: String? = null,
    val appointment_type: String = "general"
)
