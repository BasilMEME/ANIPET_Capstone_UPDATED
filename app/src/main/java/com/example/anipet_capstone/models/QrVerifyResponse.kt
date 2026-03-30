package com.example.anipet_capstone.models

data class QrVerifyResponse(
    val status: String,
    val message: String? = null,
    val application: VerifiedApplication? = null
)