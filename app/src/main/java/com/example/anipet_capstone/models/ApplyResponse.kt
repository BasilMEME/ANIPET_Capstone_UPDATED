package com.example.anipet_capstone.models

data class ApplyResponse(
    val status: String,
    val message: String,
    val application_id: String? = null,
    val qr_code: String? = null
)