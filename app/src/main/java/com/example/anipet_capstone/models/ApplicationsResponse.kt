package com.example.anipet_capstone.models

import com.example.anipet_capstone.screens.ApplicationItem

data class ApplicationsResponse(
    val status: String,
    val applications: List<ApplicationItem>
)