package com.example.anipet_capstone.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.models.VerifiedApplication
import com.example.anipet_capstone.network.ApiClient
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

@Composable
fun QrScannerScreen(
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    var statusText by remember { mutableStateOf("Tap Scan QR to verify adopter") }
    var resultData by remember { mutableStateOf<VerifiedApplication?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            scope.launch {
                try {
                    statusText = "Verifying..."
                    val res = ApiClient.api.verifyQr(result.contents)

                    if (res.status == "success" && res.application != null) {
                        statusText = "QR Verified"
                        resultData = res.application
                    } else {
                        statusText = res.message ?: "QR not found"
                        resultData = null
                    }
                } catch (e: Exception) {
                    statusText = "Error: ${e.message}"
                    resultData = null
                }
            }
        } else {
            statusText = "Scan cancelled"
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            Button(onClick = {
                val options = ScanOptions().apply {
                    setPrompt("Scan adopter QR code")
                    setBeepEnabled(true)
                    setOrientationLocked(false)
                }
                scannerLauncher.launch(options)
            }) {
                Text("Scan QR")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(statusText)

        Spacer(modifier = Modifier.height(12.dp))

        resultData?.let { app ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Verified Application", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Applicant: ${app.applicant_name}")
                    Text("Pet Name: ${app.pet_name}")
                    Text("Breed: ${app.breed}")
                    Text("Age: ${app.age}")
                    Text("Gender: ${app.gender}")
                    Text("Message: ${app.message}")
                    Text("Status: ${app.status}")
                    Text("QR Code: ${app.qr_code}")
                    Text("Date: ${app.created_at}")
                }
            }
        }
    }
}