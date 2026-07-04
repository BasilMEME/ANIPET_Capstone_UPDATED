package com.example.anipet_capstone.models

data class User(
    val id: Int,
    val full_name: String,
    val email: String,
    val username: String? = null,
    val role: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val contact_preference: String? = null
    , val is_verified: Boolean? = null
)