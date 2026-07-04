package com.example.anipet_capstone.models

data class AuthResponse(
    val status: String,
    val message: String,
    val user: User? = null
)