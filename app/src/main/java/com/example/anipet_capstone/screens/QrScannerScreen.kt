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
import androidx.compose.ui.graphics.Color
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

    AppContainer() {
        AppTopBar("QR Scanner", onBack = onBack)

        StandardCard(title = "Adoption Verification") {
            Text("Scan the adopter's QR code to verify their application.", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton("Scan QR Code", onClick = {
                val options = ScanOptions().apply {
                    setPrompt("Scan adopter QR code")
                    setBeepEnabled(true)
                    setOrientationLocked(true)
                }
                scannerLauncher.launch(options)
            })
        }

        if (statusText.isNotBlank()) {
            StandardCard {
                Text(statusText, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            }
        }

        resultData?.let { app ->
            StandardCard(title = "Verified Application") {
                InfoText("Applicant", app.applicant_name)
                InfoText("Pet", app.pet_name)
                InfoText("Breed", app.breed.orEmpty())
                InfoText("Age", app.age.orEmpty())
                InfoText("Gender", app.gender.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))
                Text("Message: ${app.message}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                InfoText("Status", app.status)
                InfoText("Date", app.created_at)
            }
        }

        SecondaryButton("Back", onClick = onBack)
    }
}