package com.example.anipet_capstone.models

data class AppointmentsResponse(
    val status: String,
    val appointments: List<Appointment>
)
