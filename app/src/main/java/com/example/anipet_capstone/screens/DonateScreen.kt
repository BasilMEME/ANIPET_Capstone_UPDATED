package com.example.anipet_capstone.screens

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.anipet_capstone.BuildConfig
import com.example.anipet_capstone.R
import com.example.anipet_capstone.models.ReturnPolicy
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun DonateScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var policy by remember { mutableStateOf<ReturnPolicy?>(null) }
    var statusText by remember { mutableStateOf("Loading donation info...") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = ApiClient.api.getReturnPolicy()
                policy = res.policy
                statusText = if (res.policy == null) "Donation info is not available yet." else ""
            } catch (e: Exception) {
                statusText = "Error loading donation info: ${e.message}"
            }
        }
    }

    val qrUrl = policy?.donation_qr_filename
        ?.takeIf { it.isNotBlank() }
        ?.let { BuildConfig.API_BASE_URL + "images/" + it }

    AppContainer {
        AppTopBar("Support AniPet", onBack = onBack)

        StandardCard(title = "Make a Donation") {
            Text(
                "Every donation helps us feed, treat, and shelter animals waiting for their forever home. " +
                    "Scan the QR code below or send directly via GCash using the details provided.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (statusText.startsWith("Error")) {
            StandardCard {
                Text(statusText, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
        } else if (policy?.donation_gcash_name.isNullOrBlank() && policy?.donation_gcash_number.isNullOrBlank() && qrUrl == null) {
            StandardCard {
                Text(
                    statusText.ifBlank { "Donation details haven't been configured yet. Please check back later." },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        } else {
            StandardCard(title = "GCash") {
                if (qrUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(qrUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Donation GCash QR code",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(R.drawable.anipet_logo),
                        error = painterResource(R.drawable.anipet_logo)
                    )
                }
                if (!policy?.donation_gcash_name.isNullOrBlank()) {
                    InfoText("Account Name", policy?.donation_gcash_name.orEmpty())
                }
                if (!policy?.donation_gcash_number.isNullOrBlank()) {
                    InfoText("GCash Number", policy?.donation_gcash_number.orEmpty())
                }
                if (!policy?.donation_notes.isNullOrBlank()) {
                    Text(
                        policy?.donation_notes.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        SecondaryButton("Back", onClick = onBack)
    }
}
