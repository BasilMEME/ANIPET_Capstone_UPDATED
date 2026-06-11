package com.example.anipet_capstone.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

fun generateQRCode(text: String): Bitmap {
    val size = 512
    val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
        }
    }

    return bitmap
}

fun getSessionUserId(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("user_id", null)
}

@Composable
fun MyApplicationsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userId = getSessionUserId(context)

    var statusText by remember { mutableStateOf("Tap Refresh to load applications") }
    var applications by remember { mutableStateOf(listOf<ApplicationItem>()) }

    if (userId == null) {
        AppContainer() {
            AppTopBar("My Applications", onBack = onBack)
            StandardCard {
                Text("User not logged in. Please log in to view your applications.", color = ComposeColor.White.copy(alpha = 0.75f))
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

    AppContainer() {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val contentModifier = if (maxWidth >= 720.dp) Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .padding(horizontal = 12.dp)
            else Modifier.fillMaxWidth()

            Column(modifier = contentModifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTopBar("My Applications", onBack = onBack)

                PrimaryButton("Refresh Applications", onClick = { loadApplications() })

                if (applications.isEmpty()) {
                    StandardCard {
                        Text("No applications yet. Start by browsing and applying for adoptable pets.", color = ComposeColor.White.copy(alpha = 0.75f))
                    }
                } else {
                    val pendingCount = applications.count { it.status.equals("pending", ignoreCase = true) }
                    val approvedCount = applications.count { it.status.equals("approved", ignoreCase = true) }
                    val rejectedCount = applications.count { it.status.equals("rejected", ignoreCase = true) }

                    StandardCard(title = "Application Summary") {
                        Text(
                            "Pending: $pendingCount • Approved: $approvedCount • Rejected: $rejectedCount",
                            color = ComposeColor.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    applications.forEach { app ->
                        StandardCard(title = "Application #${app.id}") {
                            InfoText("Pet", app.pet_name ?: app.pet_id)
                            InfoText("Breed", app.breed ?: "-")
                            InfoText("Age", app.age ?: "-")
                            InfoText("Gender", app.gender ?: "-")
                            InfoText("Applicant", app.applicant_name)
                            InfoText("Status", app.status)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Message: ${app.message}", color = ComposeColor.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Show uploaded ID documents
                            if (!app.id_documents.isNullOrEmpty()) {
                                Text("ID Document(s)", color = ComposeColor.White.copy(alpha = 0.85f))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    app.id_documents.forEach { url ->
                                        AsyncImage(model = url, contentDescription = "ID", modifier = Modifier.size(120.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Show uploaded house photos
                            if (!app.house_photos.isNullOrEmpty()) {
                                Text("House Photo(s)", color = ComposeColor.White.copy(alpha = 0.85f))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    app.house_photos.forEach { url ->
                                        AsyncImage(model = url, contentDescription = "House photo", modifier = Modifier.size(120.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (!app.qr_code.isNullOrBlank()) {
                                val qrCode = app.qr_code
                                val qrBitmap = remember(qrCode) {
                                    generateQRCode(qrCode)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.size(180.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Date: ${app.created_at}", color = ComposeColor.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)

                            if (app.status.equals("pending", ignoreCase = true)) {
                                PrimaryButton("Request Interview", onClick = {
                                    scope.launch {
                                        try {
                                            val now = java.util.Date().time.toString()
                                            val res = ApiClient.api.requestInterview(app.id, now)
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
            }
        }
    }
}