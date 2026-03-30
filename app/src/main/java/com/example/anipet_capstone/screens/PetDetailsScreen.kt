package com.example.anipet_capstone.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pet Details", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(10.dp))

        if (pet != null) {
            if (!pet!!.image.isNullOrBlank()) {
                AsyncImage(
                    model = pet!!.image,
                    contentDescription = pet!!.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(pet!!.name, style = MaterialTheme.typography.headlineSmall)
            Text("${pet!!.breed} • ${pet!!.age} • ${pet!!.gender}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(pet!!.description)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Health: ${pet!!.health_status}")
            Text("Status: ${pet!!.status}")
        } else {
            Text(statusText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = onApply) {
                Text("Apply")
            }
        }
    }
}