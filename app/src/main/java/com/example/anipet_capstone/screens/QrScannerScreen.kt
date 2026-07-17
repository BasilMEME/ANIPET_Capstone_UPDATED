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
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import androidx.compose.ui.platform.LocalContext

@Composable
fun QrScannerScreen(
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {

            try {

                    val bitmap =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                        val source = ImageDecoder.createSource(
                            context.contentResolver,
                            uri
                        )

                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }

                    } else {

                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            uri
                        )

                    }

                val width = bitmap.width
                val height = bitmap.height

                val pixels = IntArray(width * height)
                bitmap.getPixels(
                    pixels,
                    0,
                    width,
                    0,
                    0,
                    width,
                    height
                )

                val source = RGBLuminanceSource(
                    width,
                    height,
                    pixels
                )

                val binaryBitmap = BinaryBitmap(
                    HybridBinarizer(source)
                )

                try {

                    val result = MultiFormatReader().decode(binaryBitmap)

                    val qrText = result.text

                    // Show what was actually decoded
                    statusText = "Decoded: $qrText"

                    val res = ApiClient.api.verifyQr(qrText)

                    if (res.status == "success") {
                        statusText = "QR Verified"
                        resultData = res.application
                    } else {
                        statusText = res.message ?: "Verification failed"
                        resultData = null
                    }

                } catch (e: Exception) {
                    statusText = "Decode Error: ${e.message}"
                    resultData = null
                }

            } catch (e: Exception) {
                statusText = "No QR code found in image."
                resultData = null
            }
        }
    }

    AppContainer() {
        AppTopBar("QR Scanner", onBack = onBack)

        StandardCard(title = "Adoption Verification") {
            Text("Scan the adopter's QR code to verify their application.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton("Scan QR Code", onClick = {
                val options = ScanOptions().apply {
                    setPrompt("Scan adopter QR code")
                    setBeepEnabled(true)
                    setOrientationLocked(true)
                }
                scannerLauncher.launch(options)
            })
            Spacer(modifier = Modifier.height(8.dp))

            SecondaryButton(
                "Upload QR Image",
                onClick = {
                    galleryLauncher.launch("image/*")
                }
            )
        }

        if (statusText.isNotBlank()) {
            StandardCard {
                Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
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
                Text("Message: ${app.message}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                InfoText("Status", app.status)
                InfoText("Date", app.created_at)
            }
        }

        SecondaryButton("Back", onClick = onBack)
    }
}