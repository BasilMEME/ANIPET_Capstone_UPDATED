package com.example.anipet_capstone.screens

import android.content.Context
import androidx.compose.foundation.layout.*
// Removed unused Button/OutlinedButton imports
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
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

    AppContainer {
        val contentModifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .padding(horizontal = 12.dp)

        Column(modifier = contentModifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTopBar("Apply for Adoption", onBack = onBack)

                StandardCard(title = "Application Details") {
                    InfoText("Pet ID", petId)
                    InfoText("User ID", userId)
                    InfoText("Applicant", applicantName)
                }

                StandardCard(title = "Your Message") {
                    Text("Tell us why you'd like to adopt this pet.", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            focusedBorderColor = Color.White.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                PrimaryButton(
                    text = "Submit Application",
                    onClick = {
                        if (petId.isBlank() || userId.isBlank() || applicantName.isBlank()) {
                            statusText = "Missing pet ID, user ID, or applicant name"
                            return@PrimaryButton
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
                    }
                )

                SecondaryButton("Cancel", onClick = onBack)

                if (statusText.isNotBlank()) {
                    StandardCard {
                        Text(statusText, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }