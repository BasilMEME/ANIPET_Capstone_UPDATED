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
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User not logged in")
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onBack) {
                Text("Back")
            }
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

    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            Button(onClick = { loadApplications() }) {
                Text("Refresh Applications")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Status: $statusText")
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(applications) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Application #${app.id}", style = MaterialTheme.typography.titleMedium)
                        Text("Pet ID: ${app.pet_id}")
                        Text("Applicant: ${app.applicant_name}")
                        Text("Message: ${app.message}")
                        Text("Status: ${app.status}")

                        if (!app.qr_code.isNullOrBlank()) {

                            val qrBitmap = remember(app.qr_code) {
                                generateQRCode(app.qr_code!!)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(180.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Date: ${app.created_at}")
                    }
                }
            }
        }
    }
}