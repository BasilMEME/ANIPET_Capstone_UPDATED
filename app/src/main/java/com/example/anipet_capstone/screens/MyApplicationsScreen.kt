package com.example.anipet_capstone.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
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
                    applications.forEach { app ->
                        StandardCard(title = "Application #${app.id}") {
                            InfoText("Pet ID", app.pet_id)
                            InfoText("Applicant", app.applicant_name)
                            InfoText("Status", app.status)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Message: ${app.message}", color = ComposeColor.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)

                            if (!app.qr_code.isNullOrBlank()) {
                                val qrBitmap = remember(app.qr_code) {
                                    generateQRCode(app.qr_code!!)
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
                        }
                    }
                }

                SecondaryButton("Back", onClick = onBack)
            }
        }
    }
}