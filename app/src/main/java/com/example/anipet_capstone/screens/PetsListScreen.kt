package com.example.anipet_capstone.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anipet_capstone.R
import com.example.anipet_capstone.models.Pet
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun PetsListScreen(
    onPetClick: (String) -> Unit = {},
    onMyApplicationsClick: () -> Unit = {},
    onAppointmentsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onQrScannerClick: () -> Unit = {},
    onDonateClick: () -> Unit = {},
    fullName: String = "User"
) {
    val scope = rememberCoroutineScope()

    var statusText by remember { mutableStateOf("Loading pets...") }
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    fun loadPets() {
        scope.launch {
            try {
                statusText = "Loading..."
                val res = ApiClient.api.getPets()
                pets = res.pets
                statusText = if (res.pets.isEmpty()) "No adoptable pets yet" else "${res.pets.size} pets loaded"
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPets()
    }

    val filteredPets = pets.filter { pet ->
        val matchesSearch = searchQuery.isBlank() ||
            pet.name.contains(searchQuery, ignoreCase = true) ||
            pet.breed.orEmpty().contains(searchQuery, ignoreCase = true) ||
            pet.description.orEmpty().contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Available" -> pet.status.equals("available", ignoreCase = true)
            "Healthy" -> pet.health_status.orEmpty().contains("healthy", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    val availableCount = pets.count { it.status.equals("available", ignoreCase = true) }
    val healthyCount = pets.count { it.health_status.orEmpty().contains("healthy", ignoreCase = true) }
    val featuredPet = filteredPets.firstOrNull() ?: pets.firstOrNull()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1826),
                        Color(0xFF16233A),
                        Color(0xFF1E304C)
                    )
                )
            )
    ) {
        val boxScope = this
        val isWide = boxScope.maxWidth >= 840.dp

        if (isWide) {
            Row(modifier = Modifier.fillMaxSize()) {
                SidebarPanel(
                    fullName = fullName,
                    availableCount = availableCount,
                    onMyApplicationsClick = onMyApplicationsClick,
                    onQrScannerClick = onQrScannerClick,
                    onDonateClick = onDonateClick,
                    onLogoutClick = onLogoutClick,
                    onRefresh = { loadPets() },
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 320.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                )

                DashboardContent(
                    fullName = fullName,
                    statusText = statusText,
                    featuredPet = featuredPet,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    availableCount = availableCount,
                    healthyCount = healthyCount,
                    filteredPets = filteredPets,
                    onPetClick = onPetClick,
                    onRefresh = { loadPets() },
                    onMyApplicationsClick = onMyApplicationsClick,
                    onQrScannerClick = onQrScannerClick,
                    onAppointmentsClick = onAppointmentsClick,
                    onProfileClick = onProfileClick,
                    onDonateClick = onDonateClick,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 18.dp, end = 18.dp, bottom = 18.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    UserHeaderRow(fullName = fullName, onLogoutClick = onLogoutClick)
                }

                item {
                    HeroCard(
                        fullName = fullName,
                        statusText = statusText,
                        availableCount = availableCount,
                        onRefresh = { loadPets() }
                    )
                }

                item {
                    QuickActionsRow(
                        onMyApplicationsClick = onMyApplicationsClick,
                        onQrScannerClick = onQrScannerClick,
                        onAppointmentsClick = onAppointmentsClick,
                        onProfileClick = onProfileClick,
                        onDonateClick = onDonateClick,
                        onLogoutClick = onLogoutClick
                    )
                }

                item {
                    MetricsRow(
                        availableCount = availableCount,
                        healthyCount = healthyCount,
                        totalCount = pets.size
                    )
                }

                item {
                    SearchAndFilters(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                }

                item {
                    SectionHeader(
                        title = "Adoptable Pets",
                        subtitle = if (filteredPets.isEmpty()) "No pets match your current filters" else "Tap a card to open details"
                    )
                }

                if (filteredPets.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Nothing to show yet",
                            description = "Try removing the search text or switching the filter to All."
                        )
                    }
                } else {
                    items(filteredPets, key = { pet -> pet.id }) { pet ->
                        PetPreviewCard(
                            pet = pet,
                            onClick = { onPetClick(pet.id) }
                        )
                    }
                }

                item {
                    Text(
                        text = statusText,
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarPanel(
    fullName: String,
    availableCount: Int,
    onMyApplicationsClick: () -> Unit,
    onQrScannerClick: () -> Unit,
    onDonateClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        UserHeaderRow(fullName = fullName, onLogoutClick = onLogoutClick)

        HeroCard(
            fullName = fullName,
            statusText = "Ready for adoption",
            availableCount = availableCount,
            onRefresh = onRefresh
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Navigation", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SidebarActionButton(text = "My Applications", onClick = onMyApplicationsClick)
                SidebarActionButton(text = "Scan QR", onClick = onQrScannerClick)
                SidebarActionButton(text = "Donate", onClick = onDonateClick)
                SidebarActionButton(text = "Logout", onClick = onLogoutClick)
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                    RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Today", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Browse adoptable pets, send applications, and track your adoption journey.", color = Color.White.copy(alpha = 0.75f))
                AssistChip(onClick = onRefresh, label = { Text("Refresh feed") })
            }
        }
    }
}
@Composable
private fun UserHeaderRow(
    fullName: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Welcome back", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelLarge)
            Text("$fullName", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text("Logout")
        }
    }
}
@Composable
private fun DashboardContent(
    fullName: String,
    statusText: String,
    featuredPet: Pet?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    availableCount: Int,
    healthyCount: Int,
    filteredPets: List<Pet>,
    onPetClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onMyApplicationsClick: () -> Unit,
    onQrScannerClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDonateClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroCard(
                fullName = fullName,
                statusText = statusText,
                availableCount = availableCount,
                onRefresh = onRefresh
            )
        }

        item {
            QuickActionsRow(
                onMyApplicationsClick = onMyApplicationsClick,
                onQrScannerClick = onQrScannerClick,
                onAppointmentsClick = onAppointmentsClick,
                onProfileClick = onProfileClick,
                onDonateClick = onDonateClick,
                onLogoutClick = onLogoutClick
            )
        }

        item {
            MetricsRow(
                availableCount = availableCount,
                healthyCount = healthyCount,
                totalCount = filteredPets.size
            )
        }

        item {
            SearchAndFilters(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        }

        item {
            SectionHeader(
                title = "Featured Pet",
                subtitle = if (featuredPet == null) "No pets available yet" else "Tap the card to view full details"
            )
        }

        item {
            if (featuredPet == null) {
                EmptyStateCard(
                    title = "No featured pet",
                    description = "Try refreshing the feed after confirming the backend has pets." 
                )
            } else {
                PetPreviewCard(pet = featuredPet, onClick = { onPetClick(featuredPet.id) })
            }
        }

        item {
            SectionHeader(
                title = "Adoptable Pets",
                subtitle = if (filteredPets.isEmpty()) "No pets match your current filters" else "Tap a card to open details"
            )
        }

        if (filteredPets.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Nothing to show yet",
                    description = "Try removing the search text or switching the filter to All."
                )
            }
        } else {
            items(filteredPets) { pet ->
                PetPreviewCard(
                    pet = pet,
                    onClick = { onPetClick(pet.id) }
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    fullName: String,
    statusText: String,
    availableCount: Int,
    onRefresh: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFF2867E).copy(alpha = 0.28f),
                            Color(0xFFF6C9A0).copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AniPet", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "Welcome, $fullName",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Discover adoptable pets, submit an application, and track your adoption workflow.",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill(label = "Adoptable", value = availableCount.toString())
                    StatPill(label = "Status", value = statusText)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onRefresh) { Text("Refresh Pets") }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                    ) {
                        Text(
                            text = "$availableCount pets ready for adoption",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onMyApplicationsClick: () -> Unit,
    onQrScannerClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDonateClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactMode = maxWidth < 640.dp
        if (compactMode) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ActionCardButton(text = "My Applications", modifier = Modifier.fillMaxWidth(), onClick = onMyApplicationsClick)
                ActionCardButton(text = "Profile", modifier = Modifier.fillMaxWidth(), onClick = onProfileClick)
                ActionCardButton(text = "Appointments", modifier = Modifier.fillMaxWidth(), onClick = onAppointmentsClick)
                ActionCardButton(text = "Scan QR", modifier = Modifier.fillMaxWidth(), onClick = onQrScannerClick)
                ActionCardButton(text = "Donate", modifier = Modifier.fillMaxWidth(), onClick = onDonateClick)
                ActionCardButton(text = "Logout", modifier = Modifier.fillMaxWidth(), onClick = onLogoutClick)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ActionCardButton(text = "My Applications", modifier = Modifier.weight(1f), onClick = onMyApplicationsClick)
                ActionCardButton(text = "Profile", modifier = Modifier.weight(1f), onClick = onProfileClick)
                ActionCardButton(text = "Appointments", modifier = Modifier.weight(1f), onClick = onAppointmentsClick)
                ActionCardButton(text = "Scan QR", modifier = Modifier.weight(1f), onClick = onQrScannerClick)
                ActionCardButton(text = "Donate", modifier = Modifier.weight(1f), onClick = onDonateClick)
                ActionCardButton(text = "Logout", modifier = Modifier.weight(1f), onClick = onLogoutClick)
            }
        }
    }
}

@Composable
private fun MetricsRow(
    availableCount: Int,
    healthyCount: Int,
    totalCount: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard(label = "Available", value = availableCount.toString(), modifier = Modifier.weight(1f))
        MetricCard(label = "Healthy", value = healthyCount.toString(), modifier = Modifier.weight(1f))
        MetricCard(label = "Shown", value = totalCount.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.07f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Search & Filter", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search pets by name, breed, or description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = AppTextFieldColors()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = selectedFilter == "All", onClick = { onFilterSelected("All") }, label = { Text("All") })
                FilterChip(selected = selectedFilter == "Available", onClick = { onFilterSelected("Available") }, label = { Text("Available") })
                FilterChip(selected = selectedFilter == "Healthy", onClick = { onFilterSelected("Healthy") }, label = { Text("Healthy") })
            }
        }
    }
}

@Composable
private fun PetPreviewCard(
    pet: Pet,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!pet.image.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pet.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = pet.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(22.dp))
                    ,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.anipet_logo),
                    error = painterResource(R.drawable.anipet_logo)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No image", color = Color.White.copy(alpha = 0.75f))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        pet.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${pet.breed.orEmpty()} • ${pet.age.orEmpty()} • ${pet.gender.orEmpty()}",
                        color = Color.White.copy(alpha = 0.74f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }


                Surface(
                    modifier = Modifier.border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                        RoundedCornerShape(999.dp)
                    ),
                    shape = RoundedCornerShape(999.dp),
                    color = if (pet.status.equals("available", ignoreCase = true)) Color(0xFF1B998B).copy(alpha = 0.22f) else Color(0xFF8B5CF6).copy(alpha = 0.20f)
                ) {
                    Text(
                        text = pet.status.orEmpty(),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Text(
                text = pet.description.orEmpty(),
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                InfoPill("Health", pet.health_status.orEmpty())
                InfoPill("Tap", "View details")
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color.White.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.07f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.70f), style = MaterialTheme.typography.labelLarge)
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionCardButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.10f)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun SidebarActionButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.10f)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Surface(
        modifier = Modifier.border(
            BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            RoundedCornerShape(999.dp)
        ),
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.11f)
    ) {
        Text(
            text = "$label: $value",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun InfoPill(label: String, value: String) {
    Surface(
        modifier = Modifier.border(
            BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            RoundedCornerShape(999.dp)
        ),
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Text(
            text = "$label: $value",
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}