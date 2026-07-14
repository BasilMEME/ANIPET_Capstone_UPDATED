package com.example.anipet_capstone.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.R
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String, String?, String?) -> Unit,
    onGoToRegister: () -> Unit,
    onNavigateToOtp: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        AppContainer(inner) {
            Image(
                painter = painterResource(id = R.drawable.anipet_logo),
                contentDescription = "Anipet logo",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(120.dp)
                    .padding(bottom = 8.dp)
            )
            AppTopBar("Login to Anipet")

            StandardCard(title = "Credentials") {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email or Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = AppTextFieldColors()
                )
            }

            PrimaryButton(
                text = if (isLoading) "Logging in..." else "Login",
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val res = ApiClient.api.loginUser(email, password)
                            if (res.status == "success" && res.user != null) {
                                onLoginSuccess(
                                    res.user.id.toString(),
                                    res.user.full_name,
                                    res.user.email,
                                    res.user.username,
                                    res.user.role
                                )
                            } else if (res.status == "unverified") {
                                statusText = "Account not verified. Please verify using the OTP sent to your email."
                                onNavigateToOtp(email)
                            } else {
                                statusText = res.message
                            }
                        } catch (e: Exception) {
                            statusText = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            )

            SecondaryButton(
                text = "Create Account",
                onClick = onGoToRegister,
                enabled = !isLoading
            )

            if (statusText.isNotBlank()) {
                StandardCard {
                    Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }

            androidx.compose.runtime.LaunchedEffect(statusText) {
                if (statusText.isNotBlank()) scope.launch { snackbarHostState.showSnackbar(statusText) }
            }
        }
    }
}