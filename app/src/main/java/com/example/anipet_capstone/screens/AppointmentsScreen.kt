package com.example.anipet_capstone.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.models.Appointment
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun AppointmentsScreen(
    onBack: () -> Unit = {},
    onBookAppointmentClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("Loading appointments...") }
    var appointments by remember { mutableStateOf(listOf<Appointment>()) }

    fun loadAppointments() {
        scope.launch {
            val userId = getSessionUserId(context)
            if (userId == null) {
                statusText = "Please log in again to view your appointments."
                return@launch
            }
            try {
                val res = ApiClient.api.getAppointments(userId)
                statusText = res.status
                appointments = res.appointments
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAppointments()
    }

    AppContainer() {
        AppTopBar("Appointments", onBack = onBack)
        PrimaryButton("Book Appointment", onClick = onBookAppointmentClick)
        PrimaryButton("Refresh", onClick = { loadAppointments() })

        if (appointments.isEmpty()) {
            StandardCard {
                Text(statusText, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(appointments) { appointment ->
                    StandardCard(title = appointment.title) {
                        if (appointment.appointment_type == "interview") {
                            Text(
                                "🎤 Adoption Interview",
                                color = Color(0xFF64B5F6),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        InfoText("Status", appointment.status)
                        InfoText("Scheduled", appointment.scheduled_at ?: "Not set")
                        InfoText("Details", appointment.details)
                        InfoText("Created", appointment.created_at)
                    }
                }
            }
        }

        SecondaryButton("Back", onClick = onBack)
    }
}
