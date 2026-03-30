package com.example.anipet_capstone.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.anipet_capstone.models.Pet
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun PetsListScreen(
    onPetClick: (String) -> Unit = {},
    onMyApplicationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onQrScannerClick: () -> Unit = {},
    fullName: String = "User"
    
) {
    val scope = rememberCoroutineScope()

    var statusText by remember { mutableStateOf("Tap Refresh to load pets") }
    var pets by remember { mutableStateOf(listOf<Pet>()) }

    fun loadPets() {
        scope.launch {
            try {
                statusText = "Loading..."
                val res = ApiClient.api.getPets()
                statusText = res.status
                pets = res.pets
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome, $fullName", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))

        Row {
            Button(onClick = { loadPets() }) {
                Text("Refresh")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = onMyApplicationsClick) {
                Text("My Applications")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = onQrScannerClick) {
                Text("Scan QR")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = onLogoutClick) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Status: $statusText")
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(pets) { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { onPetClick(pet.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (!pet.image.isNullOrBlank()) {
                            AsyncImage(
                                model = pet.image,
                                contentDescription = pet.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Text(pet.name, style = MaterialTheme.typography.titleLarge)
                        Text("${pet.breed} • ${pet.age} • ${pet.gender}")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pet.description)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Health: ${pet.health_status}")
                        Text("Status: ${pet.status}")
                    }
                }
            }
        }
    }
}