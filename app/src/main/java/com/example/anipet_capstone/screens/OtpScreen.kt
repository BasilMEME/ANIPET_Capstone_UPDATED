package com.example.anipet_capstone.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun OtpScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var otp by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        AppContainer(inner) {
            AppTopBar("Verify OTP", onBack = onBack)

            StandardCard(title = "Enter Code") {
                Text("A verification code has been sent to your email.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )
            }

            PrimaryButton(
                text = if (isLoading) "Verifying..." else "Verify",
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val res = ApiClient.api.verifyOtp(email, otp)
                            statusText = res.message
                            if (res.status == "success") {
                                onVerified()
                            }
                        } catch (e: Exception) {
                            statusText = "Error: ${e.message}"
                        } finally { isLoading = false }
                    }
                },
                enabled = !isLoading
            )

            SecondaryButton(
                text = if (isLoading) "Resending..." else "Resend OTP",
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val res = ApiClient.api.sendOtp(email)
                            statusText = res.message
                        } catch (e: Exception) {
                            statusText = "Error: ${e.message}"
                        } finally { isLoading = false }
                    }
                },
                enabled = !isLoading
            )

            if (statusText.isNotBlank()) {
                StandardCard {
                    Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }

            LaunchedEffect(statusText) {
                if (statusText.isNotBlank()) scope.launch { snackbarHostState.showSnackbar(statusText) }
            }
        }
    }
}
