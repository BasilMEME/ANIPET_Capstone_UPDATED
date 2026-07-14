package com.example.anipet_capstone.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    onNavigateToOtp: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactPref by remember { mutableStateOf("Email") }
    var statusText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        AppContainer(inner) {
            AppTopBar("Create Account")

            StandardCard(title = "Account Details") {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                OutlinedTextField(
                    value = middleName,
                    onValueChange = { middleName = it },
                    label = { Text("Middle Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                OutlinedTextField(
                    value = suffix,
                    onValueChange = { suffix = it },
                    label = { Text("Suffix (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Preferred contact method",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = contactPref == "Email", onClick = { contactPref = "Email" })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Email", color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = contactPref == "Phone", onClick = { contactPref = "Phone" })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Phone", color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
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

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = AppTextFieldColors()
                )
            }

            PrimaryButton(
                text = if (isLoading) "Registering..." else "Register",
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            if (firstName.isBlank() || lastName.isBlank()) {
                                statusText = "First and Last name are required"
                            } else if (password != confirmPassword) {
                                statusText = "Passwords do not match"
                            } else {
                                val res = ApiClient.api.registerUser(
                                        username = username,
                                        firstName = firstName,
                                        middleName = middleName,
                                        lastName = lastName,
                                        suffix = suffix,
                                        email = email,
                                        phone = phone,
                                        address = address,
                                        contactPreference = contactPref.lowercase(),
                                        password = password,
                                        confirmPassword = confirmPassword
                                    )
                                statusText = res.message
                                if (res.status == "success") {
                                    val otpRes = try {
                                        ApiClient.api.sendOtp(email)
                                    } catch (e: Exception) {
                                        statusText = "Registration succeeded, but OTP send failed: ${e.message}"
                                        null
                                    }

                                    if (otpRes != null) {
                                        statusText = otpRes.message
                                    }
                                    onNavigateToOtp(email)
                                }
                            }
                        } catch (e: Exception) {
                            statusText = "Error: ${e.message}"
                        } finally { isLoading = false }
                    }
                },
                enabled = !isLoading
            )

            SecondaryButton(
                text = "Back to Login",
                onClick = onBackToLogin,
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
