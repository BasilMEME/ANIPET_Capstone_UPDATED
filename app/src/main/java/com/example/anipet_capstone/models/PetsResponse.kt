package com.example.anipet_capstone.models

data class PetsResponse(
    val status: String,
    val pets: List<Pet>
)