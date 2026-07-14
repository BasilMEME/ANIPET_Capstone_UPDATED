package com.example.anipet_capstone.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun BookAppointmentScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var scheduledAt by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun submitAppointment() {
        val userId = getSessionUserId(context)
        if (userId == null) {
            statusText = "Please log in again to book an appointment."
            return
        }
        if (title.isBlank() || scheduledAt.isBlank()) {
            statusText = "Please fill in the title and scheduled date/time."
            return
        }
        scope.launch {
            isLoading = true
            try {
                val res = ApiClient.api.bookAppointment(userId, title, details, scheduledAt, null)
                statusText = res.message
                if (res.status == "success") {
                    onBack()
                }
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    AppContainer() {
        AppTopBar("Book Appointment", onBack = onBack)

        StandardCard(title = "Appointment Details") {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppTextFieldColors()
            )
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = AppTextFieldColors()
            )
            OutlinedTextField(
                value = scheduledAt,
                onValueChange = { scheduledAt = it },
                label = { Text("Scheduled Date/Time (YYYY-MM-DD HH:MM:SS)") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppTextFieldColors()
            )
        }

        PrimaryButton(
            text = if (isLoading) "Booking..." else "Book Appointment",
            onClick = { submitAppointment() },
            enabled = !isLoading
        )

        if (statusText.isNotBlank()) {
            StandardCard {
                Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
            }
        }

        SecondaryButton("Back", onClick = onBack)
    }
}
