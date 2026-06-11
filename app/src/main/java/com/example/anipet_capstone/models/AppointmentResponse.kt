package com.example.anipet_capstone.models

data class AppointmentResponse(
    val status: String,
    val message: String,
    val appointment_id: String? = null
)
