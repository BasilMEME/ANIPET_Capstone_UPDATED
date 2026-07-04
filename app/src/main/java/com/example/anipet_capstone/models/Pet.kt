package com.example.anipet_capstone.models

data class Pet(
    val id: String,
    val name: String,
    val breed: String?,
    val age: String?,
    val gender: String?,
    val description: String?,
    val health_status: String?,
    val image: String?,
    val status: String?
)