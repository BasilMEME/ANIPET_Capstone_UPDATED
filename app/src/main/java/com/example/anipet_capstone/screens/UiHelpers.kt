package com.example.anipet_capstone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AppContainer(innerPadding: PaddingValues = PaddingValues(0.dp), content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(18.dp)
            .padding(innerPadding)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), MaterialTheme.shapes.large)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (onBack != null) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Back", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun AppTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.04f)
)

@Composable
fun StandardCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                MaterialTheme.shapes.large
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (title != null) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun InfoText(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label:",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.65f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


