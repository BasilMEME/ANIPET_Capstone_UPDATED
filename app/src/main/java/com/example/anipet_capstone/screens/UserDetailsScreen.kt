package com.example.anipet_capstone.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anipet_capstone.network.ApiClient
import com.example.anipet_capstone.models.UserProfileResponse
import kotlinx.coroutines.launch

@Composable
fun UserDetailsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var profileData by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = getSessionUserId(context) ?: run {
                    errorMessage = "User not logged in"
                    isLoading = false
                    return@launch
                }
                val response = ApiClient.api.getUserProfile(userId)
                profileData = response
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error loading profile: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFFF6B6B))
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF6B6B))
            }
        } else if (errorMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFF8B0000), shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(errorMessage, color = Color.White, fontSize = 14.sp)
            }
        } else if (profileData?.user != null) {
            val user = profileData!!.user!!

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        user.full_name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "@${user.username ?: "user"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip("Member", Color(0xFFFF6B6B))
                        if (user.role == "admin") {
                            StatusChip("Admin", Color(0xFF4CAF50))
                        }
                        if (user.is_verified == true) {
                            StatusChip("Verified", Color(0xFF2196F3))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact Information
            ProfileSection(title = "Contact Information") {
                ProfileInfoRow(label = "Email", value = user.email)
                ProfileInfoRow(label = "Phone", value = user.phone ?: "Not provided")
                ProfileInfoRow(label = "Contact Preference", value = user.contact_preference ?: "Not set")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address & Location
            if (!user.address.isNullOrEmpty()) {
                ProfileSection(title = "Location") {
                    ProfileInfoRow(label = "Address", value = user.address!!)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Account Information
            ProfileSection(title = "Account Information") {
                ProfileInfoRow(label = "User ID", value = user.id.toString())
                ProfileInfoRow(label = "Role", value = user.role ?: "User")
                ProfileInfoRow(
                    label = "Account Status",
                    value = if (user.is_verified == true) "Verified" else "Unverified"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton("Edit Profile", onClick = onBack)
                SecondaryButton("Back to Home", onClick = onBack)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF6B6B)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun StatusChip(label: String, bgColor: Color) {
    Surface(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(6.dp),
        color = bgColor.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, bgColor)
    ) {
        Text(
            label,
            modifier = Modifier.padding(6.dp, 3.dp),
            fontSize = 10.sp,
            color = bgColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// session helpers moved to AppSession.kt
