package com.example.anipet_capstone.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.anipet_capstone.R
import coil.compose.AsyncImage
import com.example.anipet_capstone.models.Pet
import com.example.anipet_capstone.network.ApiClient

@Composable
fun PetDetailsScreen(
    petId: String,
    onBack: () -> Unit,
    onApply: () -> Unit
) {
    var statusText by remember { mutableStateOf("Loading...") }
    var pet by remember { mutableStateOf<Pet?>(null) }

    LaunchedEffect(petId) {
        try {
            val res = ApiClient.api.getPet(petId)
            statusText = res.status
            pet = res.pet
        } catch (e: Exception) {
            statusText = "Error: ${e.message}"
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
                AppTopBar("Pet Details", onBack = onBack)

                if (pet != null) {
                    val p = pet!!
                    StandardCard {
                        if (!p.image.isNullOrBlank()) {
                            AsyncImage(
                                model = p.image,
                                contentDescription = p.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(22.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.anipet_logo),
                                error = painterResource(R.drawable.anipet_logo)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(p.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("${p.breed.orEmpty()} • ${p.age.orEmpty()} • ${p.gender.orEmpty()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(p.description.orEmpty(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoText("Health", p.health_status.orEmpty())
                        InfoText("Status", p.status.orEmpty())
                    }
                } else {
                    StandardCard {
                        Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    }
                }

                PrimaryButton("Apply for Adoption", onClick = onApply)
                SecondaryButton("Back", onClick = onBack)
            }
        }
    }
}