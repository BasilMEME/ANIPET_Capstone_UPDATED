package com.example.anipet_capstone.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

fun getSavedFullName(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("full_name", null)
}

fun getSavedUserId(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("user_id", null)
}

@Composable
fun ApplyAdoptionScreen(
    petId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val applicantName = getSavedFullName(context) ?: ""
    val userId = getSavedUserId(context) ?: ""

    var message by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Apply Adoption", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Pet ID: $petId")
        Text("User ID: $userId")
        Text("Applicant: $applicantName")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = {
                if (petId.isBlank() || userId.isBlank() || applicantName.isBlank()) {
                    statusText = "Missing pet ID, user ID, or applicant name"
                    return@Button
                }

                scope.launch {
                    try {
                        val res = ApiClient.api.applyAdoption(
                            petId = petId,
                            userId = userId,
                            applicantName = applicantName,
                            message = message
                        )
                        statusText = res.message
                    } catch (e: Exception) {
                        statusText = "Error: ${e.message}"
                    }
                }
            }) {
                Text("Submit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(statusText)
    }
}