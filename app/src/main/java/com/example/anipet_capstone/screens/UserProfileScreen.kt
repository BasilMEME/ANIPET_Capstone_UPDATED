package com.example.anipet_capstone.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import com.example.anipet_capstone.models.UserProfileResponse
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("Loading profile...") }
    var profile by remember { mutableStateOf<UserProfileResponse?>(null) }

    fun loadProfile() {
        scope.launch {
            try {
                val userId = getSessionUserId(context) ?: return@launch
                val res = ApiClient.api.getUserProfile(userId)
                statusText = res.status
                profile = res
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    AppContainer() {
        AppTopBar("Profile", onBack = onBack)

        profile?.user?.let { user ->
            StandardCard(title = "Account Details") {
                InfoText("Full Name", user.full_name)
                InfoText("Username", user.username ?: "-")
                InfoText("Email", user.email)
                if (!user.address.isNullOrEmpty()) InfoText("Address", user.address ?: "-")
                if (!user.phone.isNullOrEmpty()) InfoText("Phone", user.phone ?: "-")
                if (!user.contact_preference.isNullOrEmpty()) InfoText("Contact Pref", user.contact_preference ?: "-")
                InfoText("Role", user.role ?: "user")
                InfoText("Verified", if (user.role == "user") "Yes" else "No")
            }
        } ?: run {
            StandardCard {
                Text(statusText, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
            }
        }

        SecondaryButton("Back", onClick = onBack)
    }
}
