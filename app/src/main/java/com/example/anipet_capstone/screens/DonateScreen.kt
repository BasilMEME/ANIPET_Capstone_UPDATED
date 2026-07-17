package com.example.anipet_capstone.screens

import android.content.Context
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.fillMaxSize
import com.example.anipet_capstone.models.DonationResponse
import android.util.Log
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.compose.ui.platform.LocalContext




@Composable
fun DonateScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var policy by remember { mutableStateOf<ReturnPolicy?>(null) }
    var statusText by remember { mutableStateOf("Loading donation info...") }

    var showDonationDialog by rememberSaveable { mutableStateOf(false) }
    var showPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var petName by rememberSaveable { mutableStateOf("") }
    var donorName by rememberSaveable { mutableStateOf("") }
    var donationAmount by rememberSaveable { mutableStateOf("") }
    var referenceNumber by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var donationSuccess by rememberSaveable {

        mutableStateOf(false)

    }

    val context: android.content.Context = LocalContext.current

    val prefs = context.getSharedPreferences(
        "AniPetPrefs",
        Context.MODE_PRIVATE
    )
    val userId = prefs.getString("user_id", "") ?: ""
    Log.d("DONATION", "userId = $userId")
    val userEmail = prefs.getString("email", "") ?: ""

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedReceiptUri = uri
    }

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

        if (showDonationDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text("Donation Details")
                },
                text = {
                    Column {
                        Text(
                            "Please provide your donation information before proceeding to the payment page."
                        )

                        Spacer(modifier = Modifier.height(15.dp))
                        OutlinedTextField(
                            value = petName,
                            onValueChange = {
                                petName = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Pet Name (Optional)")
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = donorName,
                            onValueChange = {
                                donorName = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Donor Name *")
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = donationAmount,
                            onValueChange = {
                                donationAmount = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Donation Amount (₱) *")
                            }
                        )

                        if (errorMessage.isNotEmpty()) {

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                errorMessage,
                                color = Color.Red
                            )
                        }

                    }

                },

                confirmButton = {
                    Button(
                        onClick = {
                            if (donorName.isBlank()) {
                                errorMessage = "Please enter the donor's name."
                            } else if (donationAmount.isBlank()) {
                                errorMessage = "Please enter the donation amount."
                            } else {
                                errorMessage = ""
                                showDonationDialog = false
                                showPaymentDialog = true
                            }
                        }
                    ) {
                        Text("Continue")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            showDonationDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showPaymentDialog) {

            AlertDialog(

                onDismissRequest = { },

                title = {
                    Text("Complete Your Donation")
                },

                text = {

                    Column {

                        Text(
                            "Please scan the GCash QR Code below to complete your donation."
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        if (qrUrl != null) {

                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(qrUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Donation QR",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(R.drawable.anipet_logo),
                                error = painterResource(R.drawable.anipet_logo)
                            )

                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        if (!policy?.donation_gcash_name.isNullOrBlank()) {
                            InfoText(
                                "Account Name",
                                policy?.donation_gcash_name.orEmpty()
                            )
                        }

                        if (!policy?.donation_gcash_number.isNullOrBlank()) {
                            InfoText(
                                "GCash Number",
                                policy?.donation_gcash_number.orEmpty()
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = referenceNumber,
                            onValueChange = {
                                referenceNumber = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Reference Number *")
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                launcher.launch("image/*")
                            }
                        ) {
                            Text(
                                if (selectedReceiptUri == null)
                                    "Upload Receipt (Optional)"
                                else
                                    "Receipt Selected"
                            )
                        }

                        if (!errorMessage.isNullOrEmpty()) {

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                errorMessage,
                                color = Color.Red
                            )
                        }
                    }
                },

                confirmButton = {

                    Button(
                        onClick = {
                            if (referenceNumber.isBlank()) {
                                errorMessage = "Reference number is required."
                                return@Button
                            }
                            scope.launch {
                                try {

                                    val receiptPart = selectedReceiptUri?.let { uri ->

                                        val bytes = context.contentResolver
                                            .openInputStream(uri)
                                            ?.use { it.readBytes() }
                                            ?: ByteArray(0)

                                        val requestBody = bytes.toRequestBody(
                                            "image/*".toMediaTypeOrNull()
                                        )

                                        MultipartBody.Part.createFormData(
                                            "receipt",
                                            "receipt.jpg",
                                            requestBody
                                        )
                                    }

                                    val response = ApiClient.api.submitDonation(
                                        userId = userId.toRequestBody("text/plain".toMediaType()),
                                        donorName = donorName.toRequestBody("text/plain".toMediaType()),
                                        petName = petName.toRequestBody("text/plain".toMediaType()),
                                        amount = donationAmount.toRequestBody("text/plain".toMediaType()),
                                        referenceNumber = referenceNumber.toRequestBody("text/plain".toMediaType()),
                                        paymentMethod = "GCash".toRequestBody("text/plain".toMediaType()),
                                        receipt = receiptPart
                                    )

                                    if (response.success) {
                                        showPaymentDialog = false
                                        donationSuccess = true
                                        errorMessage = ""
                                    } else {
                                        errorMessage = response.message ?: "Unknown server error."
                                    }

                                } catch (e: Exception) {
                                    Log.e("DONATION", "ERROR", e)
                                    errorMessage = e.localizedMessage ?: "Unable to connect to the server."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Donation")
                    }

                },

                dismissButton = {

                    TextButton(

                        onClick = {

                            showPaymentDialog = false

                        }

                    ) {

                        Text("Cancel")

                    }

                }

            )

        }

        if (donationSuccess) {

            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text("Thank You!")
                },
                text = {
                    Text(
                        "Your donation has been submitted successfully. It is now waiting for verification by the AniPet administrator."
                    )
                },

                confirmButton = {
                    Button(
                        onClick = {

                            donationSuccess = false

                            // Return to the Complete Donation dialog
                            showPaymentDialog = true

                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        StandardCard(title = "Support AniPet") {

            Text(
                text =
                    "Thank you for considering a donation to AniPet.\n\n" +
                            "Your generosity helps provide rescued animals with food, shelter, medical treatment, vaccinations, and daily care while they wait for their forever homes.\n\n" +
                            "Every contribution, no matter how small, makes a meaningful difference in improving the lives of our rescued animals.\n\n" +
                            "Refund Policy\n\n" +
                            "Donations may be refunded within 48 hours from the time of submission provided that they have not yet been allocated for shelter operations. After the 48-hour grace period, donations become non-refundable.\n\n" +
                            "By continuing, you acknowledge and agree to this donation and refund policy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showDonationDialog = true
                }
            ) {
                Text("Continue Donation")
            }

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Cancel",
                onClick = onBack
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
        }
    }
}

