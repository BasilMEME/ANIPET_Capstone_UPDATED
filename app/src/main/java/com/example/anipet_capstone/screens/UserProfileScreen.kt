package com.example.anipet_capstone.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import com.example.anipet_capstone.models.UserProfileResponse
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("Loading profile...") }
    var profile by remember { mutableStateOf<UserProfileResponse?>(null) }

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf("") }

    var editFullName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editContactPref by remember { mutableStateOf("Email") }

    fun loadProfile() {
        scope.launch {
            val userId = getSessionUserId(context)
            if (userId == null) {
                statusText = "Please log in again to view your profile."
                return@launch
            }
            try {
                val res = ApiClient.api.getUserProfile(userId)
                statusText = res.status
                profile = res
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
            }
        }
    }

    fun startEditing() {
        val user = profile?.user ?: return
        editFullName = user.full_name
        editPhone = user.phone.orEmpty()
        editAddress = user.address.orEmpty()
        editContactPref = user.contact_preference
            ?.replaceFirstChar { it.uppercase() }
            ?.takeIf { it == "Email" || it == "Phone" }
            ?: "Email"
        saveStatus = ""
        isEditing = true
    }

    fun saveProfile() {
        val userId = getSessionUserId(context) ?: return
        if (editFullName.isBlank()) {
            saveStatus = "Full name is required."
            return
        }
        scope.launch {
            isSaving = true
            saveStatus = ""
            try {
                val res = ApiClient.api.updateUserProfile(
                    userId = userId,
                    fullName = editFullName,
                    phone = editPhone,
                    address = editAddress,
                    contactPreference = editContactPref
                )
                if (res.status == "success") {
                    profile = res
                    isEditing = false
                    saveStatus = "Profile updated!"
                } else {
                    saveStatus = "Update failed. Please try again."
                }
            } catch (e: Exception) {
                saveStatus = "Error: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    AppContainer() {
        AppTopBar("Profile", onBack = onBack)

        profile?.user?.let { user ->
            if (!isEditing) {
                StandardCard(title = "Account Details") {
                    InfoText("Full Name", user.full_name)
                    InfoText("Username", user.username ?: "-")
                    InfoText("Email", user.email)
                    if (!user.address.isNullOrEmpty()) InfoText("Address", user.address ?: "-")
                    if (!user.phone.isNullOrEmpty()) InfoText("Phone", user.phone ?: "-")
                    if (!user.contact_preference.isNullOrEmpty()) InfoText("Contact Pref", user.contact_preference ?: "-")
                    InfoText("Role", user.role ?: "user")
                    InfoText("Verified", if (user.is_verified == true) "Yes" else "No")
                }

                if (saveStatus.isNotBlank()) {
                    StandardCard {
                        Text(saveStatus, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                PrimaryButton("Edit Profile", onClick = { startEditing() })
                SecondaryButton("Back", onClick = onBack)
            } else {
                StandardCard(title = "Edit Profile") {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = user.email,
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = AppTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Preferred contact method",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = editContactPref == "Email", onClick = { editContactPref = "Email" })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = editContactPref == "Phone", onClick = { editContactPref = "Phone" })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Phone", color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (saveStatus.isNotBlank()) {
                    StandardCard {
                        Text(saveStatus, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                PrimaryButton(if (isSaving) "Saving..." else "Save Changes", onClick = { saveProfile() }, enabled = !isSaving)
                SecondaryButton("Cancel", onClick = { isEditing = false; saveStatus = "" }, enabled = !isSaving)
            }
        } ?: run {
            StandardCard {
                Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
            }
            SecondaryButton("Back", onClick = onBack)
        }
    }
}
