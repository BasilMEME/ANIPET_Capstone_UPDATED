package com.example.anipet_capstone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anipet_capstone.models.ApplicationTrackingData
import com.example.anipet_capstone.models.ApplicationProgress
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationTrackingScreen(
    applicationId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var appData by remember { mutableStateOf<ApplicationTrackingData?>(null) }
    var progress by remember { mutableStateOf<ApplicationProgress?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(applicationId) {
        scope.launch {
            try {
                val response = ApiClient.api.getApplicationStatus(applicationId)
                if (response.success) {
                    appData = response.data
                    progress = response.progress
                } else {
                    errorMessage = response.message ?: "Failed to load"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load application"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        TopAppBar(
            title = { Text("Application Tracking") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFFF6B6B)
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFFFEBEE), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(errorMessage, color = Color(0xFFD32F2F))
            }
        } else if (appData != null) {
            appData?.let { application ->
                val progressData = progress

            // Pet Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    if (!application.pet_image.isNullOrEmpty()) {
                        AsyncImage(
                            model = application.pet_image,
                            contentDescription = "Pet",
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.Gray, shape = MaterialTheme.shapes.small)
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = application.pet_name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Application #${application.id}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Applicant: ${application.applicant_name}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Current Status
            if (progressData != null) {
                val currentStatus = progressData.current_status
                val statuses = progressData.statuses
                val completedSteps = progressData.completed_steps

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Current Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatStatusText(currentStatus),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getStatusColorValue(currentStatus)
                        )

                        // Status Description
                        Text(
                            text = getStatusDescText(currentStatus),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = (completedSteps.toFloat() / statuses.size),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .height(6.dp),
                            color = getStatusColorValue(currentStatus),
                            trackColor = Color.LightGray
                        )
                        Text(
                            text = "Step $completedSteps of ${statuses.size}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // QR Code Section (if approved)
            val qrCode = appData?.qr_code
            if (!qrCode.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, Color.LightGray, shape = MaterialTheme.shapes.medium),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tracking QR Code",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = qrCode,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(180.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Scan to track your pet's adoption journey",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "💚 Support AniPet",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Scan QR Ph code to donate via GCash/PayMaya",
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // Admin Notes (if available)
            if (!appData?.admin_notes.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E5F5)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Admin Notes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = appData?.admin_notes ?: "",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Timeline/Steps
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Application Steps",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val steps = listOf(
                        "pending" to "Application Submitted",
                        "screening" to "Under Review",
                        "approved" to "Approved by Admin",
                        "for_releasing" to "For Release",
                        "ready_pickup" to "Ready for Pick-up",
                        "completed" to "Adoption Complete"
                    )

                    steps.forEachIndexed { index, (status, label) ->
                        ApplicationStepItem(
                            label = label,
                            isCompleted = isApplicationStepCompleted(
                                status,
                                appData?.status ?: ""
                            ),
                            isLast = index == steps.size - 1
                        )
                    }
                }
            }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ApplicationStepItem(
    label: String,
    isCompleted: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isCompleted) Color(0xFF4CAF50) else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50) else Color.LightGray
                        )
                )
            }
        }
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
            fontSize = 14.sp,
            color = if (isCompleted) Color.Black else Color.Gray
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}

private fun isApplicationStepCompleted(stepStatus: String, currentStatus: String): Boolean {
    val statusOrder = listOf(
        "pending",
        "screening",
        "approved",
        "for_releasing",
        "ready_pickup",
        "completed"
    )
    val currentIndex = statusOrder.indexOf(currentStatus)
    val stepIndex = statusOrder.indexOf(stepStatus)

    if (currentStatus == "rejected") return false
    return stepIndex <= currentIndex
}

private fun formatStatusText(status: String): String {
    return when (status) {
        "pending" -> "Pending Review"
        "screening" -> "Under Screening"
        "approved" -> "Approved"
        "for_releasing" -> "For Release"
        "ready_pickup" -> "Ready for Pick-up"
        "completed" -> "Completed"
        "rejected" -> "Rejected"
        else -> status.replaceFirstChar { it.uppercase() }
    }
}

private fun getStatusDescText(status: String): String {
    return when (status) {
        "pending" -> "Your application is waiting to be reviewed by our team"
        "screening" -> "We are reviewing your application"
        "approved" -> "Congratulations! Your application has been approved"
        "for_releasing" -> "Your pet is ready to be released to you"
        "ready_pickup" -> "Your pet is ready for pick-up"
        "completed" -> "You have successfully adopted your pet"
        "rejected" -> "Unfortunately, your application was not approved"
        else -> "Status update pending"
    }
}

private fun getStatusColorValue(status: String): Color {
    return when (status) {
        "pending" -> Color(0xFFFFA500)
        "screening" -> Color(0xFF2196F3)
        "approved" -> Color(0xFF4CAF50)
        "for_releasing" -> Color(0xFF9C27B0)
        "ready_pickup" -> Color(0xFF00BCD4)
        "completed" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}
