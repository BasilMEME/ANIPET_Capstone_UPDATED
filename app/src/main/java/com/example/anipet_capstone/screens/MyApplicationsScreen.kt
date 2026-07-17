package com.example.anipet_capstone.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MyApplicationsScreen(
    onBack: () -> Unit = {},
    onTrackClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = getSessionUserId(context)

    var statusText by remember { mutableStateOf("Tap Refresh to load applications") }
    var applications by remember { mutableStateOf(listOf<ApplicationItem>()) }
    var showReturnDialog by remember { mutableStateOf(false) }
    var selectedReturnApplication by remember { mutableStateOf<ApplicationItem?>(null) }
    var returnReason by remember { mutableStateOf("") }
    var penaltyAmount by remember { mutableStateOf("1000.00") }
    var returnStatusText by remember { mutableStateOf("") }

    if (userId == null) {
        AppContainer {
            AppTopBar("My Applications", onBack = onBack)
            StandardCard {
                Text("User not logged in. Please log in to view your applications.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
            }
            SecondaryButton("Back", onClick = onBack)
        }
        return
    }

    fun loadApplications() {
        scope.launch {
            try {
                statusText = "Loading..."
                val res = ApiClient.api.getApplications(userId)
                statusText = res.status
                applications = res.applications
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
            }
        }
    }

    LaunchedEffect(userId) {
        loadApplications()
    }

    AppContainer {
        val metrics = LocalContext.current.resources.displayMetrics
        val screenWidthDp = metrics.widthPixels / metrics.density
        val isWide = screenWidthDp >= 720.0
        val contentModifier = if (isWide) Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .padding(horizontal = 12.dp)
        else Modifier.fillMaxWidth()

        Column(modifier = contentModifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTopBar("My Applications", onBack = onBack)

            PrimaryButton("Refresh Applications", onClick = { loadApplications() })

            if (applications.isEmpty()) {
                StandardCard {
                    Text(
                        "No applications yet. Start by browsing and applying for adoptable pets.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            } else {
                val pendingCount = applications.count { it.status.equals("pending", ignoreCase = true) }
                val approvedCount = applications.count { it.status.equals("approved", ignoreCase = true) }
                val rejectedCount = applications.count { it.status.equals("rejected", ignoreCase = true) }

                StandardCard(title = "Application Summary") {
                    Text(
                        "Pending: $pendingCount • Approved: $approvedCount • Rejected: $rejectedCount",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                applications.forEach { app ->

                    val displayedPetName =
                        app.pet_name.takeUnless { it.isNullOrBlank() }
                            ?: app.pet_id

                    StandardCard(
                        title = "Application for $displayedPetName"
                    ) {

                        if (!app.pet_image.isNullOrBlank()) {
                                AsyncImage(
                                    model = app.pet_image,
                                    contentDescription = displayedPetName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(ComposeColor(0xFFFCEAE6), shape = RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            InfoText("Pet", displayedPetName)
                            InfoText("Applicant", app.applicant_name)

                            Spacer(modifier = Modifier.height(12.dp))

                            AdoptionProgressTracker(app.status)

                            Spacer(modifier = Modifier.height(12.dp))

                            InfoText("Status", app.status)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Message: ${app.message}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )

                            if (!app.qr_code.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                PrimaryButton("Track Application", onClick = { onTrackClick(app.id) })
                            }

                            app.qr_code?.let { qrValue ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ComposeColor(0xFFFCEAE6), shape = RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Adoption Tracking QR",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AsyncImage(
                                        model = qrValue,
                                        contentDescription = "QR Code",
                                        modifier = Modifier.size(160.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Show this QR when visiting the shelter",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp),
                                        color = ComposeColor(0xFF4CAF50).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, ComposeColor(0xFF4CAF50))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                "💚 Support AniPet",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Scan QR Ph code to donate via GCash/PayMaya",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Date: ${app.created_at}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.labelSmall
                            )

                            if (!app.return_request_status.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoText("Return Request", app.return_request_status)
                                app.return_request_reason?.let { InfoText("Reason", it) }
                                app.penalty_amount?.let { InfoText("Penalty", "₱${String.format(Locale.getDefault(), "%.2f", it)}") }
                                InfoText("Penalty paid", if (app.penalty_paid == 1) "Yes" else "No")
                            } else if (
                                app.status.equals("completed", ignoreCase = true) ||
                                app.status.equals("ready_pickup", ignoreCase = true) ||
                                app.status.equals("approved", ignoreCase = true)
                            ) {
                                PrimaryButton("Request Return", onClick = {
                                    selectedReturnApplication = app
                                    returnReason = ""
                                    returnStatusText = ""
                                    showReturnDialog = true
                                    scope.launch {
                                        try {
                                            val policyResponse = ApiClient.api.getReturnPolicy()
                                            policyResponse.policy?.computed_penalty?.let { amount ->
                                                penaltyAmount = String.format(Locale.getDefault(), "%.2f", amount)
                                            }
                                        } catch (e: Exception) {
                                            // Keep the fallback amount; server still authoritatively computes the real charge.
                                        }
                                    }
                                })
                            }

                            if (app.status.equals("pending", ignoreCase = true)) {
                                PrimaryButton("Request Interview", onClick = {
                                    scope.launch {
                                        try {
                                            val res = ApiClient.api.requestInterview(app.id)
                                            statusText = res.message
                                            loadApplications()
                                        } catch (e: Exception) {
                                            statusText = "Error: ${e.message}"
                                        }
                                    }
                                })
                            }
                        }
                    }
                }

            SecondaryButton("Back", onClick = onBack)

            if (showReturnDialog && selectedReturnApplication != null) {
                AlertDialog(
                    onDismissRequest = { showReturnDialog = false },
                    title = { Text("Return Request") },
                    text = {
                        Column {
                            Text("Request a pet return for application #${selectedReturnApplication!!.id}.")
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = returnReason,
                                onValueChange = { returnReason = it },
                                label = { Text("Reason for return") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "A standard return penalty of ₱$penaltyAmount applies, per shelter policy.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (returnStatusText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(returnStatusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (returnReason.isBlank()) {
                                returnStatusText = "Please enter a reason for the return request."
                                return@TextButton
                            }
                            showReturnDialog = false
                            selectedReturnApplication?.let { app ->
                                scope.launch {
                                    try {
                                        val res = ApiClient.api.requestReturn(
                                            applicationId = app.id,
                                            userId = userId,
                                            petId = app.pet_id,
                                            reason = returnReason,
                                            penaltyAmount = penaltyAmount
                                        )
                                        returnStatusText = res.message
                                        loadApplications()
                                    } catch (e: Exception) {
                                        returnStatusText = "Error: ${e.message}"
                                    }
                                }
                            }
                        }) {
                            Text("Submit")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReturnDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdoptionProgressTracker(status: String) {

    val steps = listOf(
        "pending",
        "screening",
        "approved",
        "for_releasing",
        "ready_pickup",
        "completed"
    )

    val currentIndex = steps.indexOf(status.lowercase()).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {

        Text(
            text = "📍 Adoption Progress",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, _ ->
                Text(
                    if (index <= currentIndex) "🟢" else "⚪",
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("🏠","🔍","✅","📦","📅","❤️").forEach {
                Text(it)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                "Submitted",
                "Screening",
                "Approved",
                "Release",
                "Pickup",
                "Completed"
            ).forEach {
                Text(
                    text = it,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Current Status",
            fontWeight = FontWeight.Bold
        )

        Text(
            text = status.replace("_", " ")
                .split(" ")
                .joinToString(" ") {
                    it.replaceFirstChar { c -> c.uppercase() }
                }
        )
    }
}
