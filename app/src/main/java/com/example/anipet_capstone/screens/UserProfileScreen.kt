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
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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

    var showPhotoDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    // Locally staged photo — only uploaded when Save Changes is pressed
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }

    fun resetPendingPhoto() {
        pendingImageUri = null
    }

    suspend fun uploadStagedPhoto(uri: Uri): UserProfileResponse? {
        val userId = getSessionUserId(context) ?: return null
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File.createTempFile("profile_", ".jpg", context.cacheDir)
            file.outputStream().use { out -> inputStream?.copyTo(out) }

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            ApiClient.api.uploadProfilePicture(userIdBody, imagePart)
        } catch (e: Exception) {
            null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pendingImageUri = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageUri?.let { pendingImageUri = it }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val imagesDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
            val file = File.createTempFile("capture_", ".jpg", imagesDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val imagesDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
            val file = File.createTempFile("capture_", ".jpg", imagesDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

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
        resetPendingPhoto()
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
                // Upload the staged photo first, if one was picked
                if (pendingImageUri != null) {
                    val photoRes = uploadStagedPhoto(pendingImageUri!!)
                    if (photoRes?.status != "success") {
                        saveStatus = "Failed to upload photo. Please try again."
                        isSaving = false
                        return@launch
                    }
                    profile = photoRes
                }

                val res = ApiClient.api.updateUserProfile(
                    userId = userId,
                    fullName = editFullName,
                    phone = editPhone,
                    address = editAddress,
                    contactPreference = editContactPref
                )
                if (res.status == "success") {
                    profile = res
                    resetPendingPhoto()
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

    fun handleBack() {
        resetPendingPhoto()
        onBack()
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    AppContainer() {
        AppTopBar("Profile", onBack = { handleBack() })

        profile?.user?.let { user ->
            StandardCard {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = pendingImageUri ?: (user.profile_picture_url ?: ""),
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .let { m -> if (isEditing) m.clickable { showPhotoDialog = true } else m }
                        )
                        if (isEditing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Change Photo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showPhotoDialog = true }
                            )
                        }
                    }
                }
            }

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
                SecondaryButton("Back", onClick = { handleBack() })
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
                SecondaryButton("Cancel", onClick = { resetPendingPhoto(); isEditing = false; saveStatus = "" }, enabled = !isSaving)
            }
        } ?: run {
            StandardCard {
                Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
            }
            SecondaryButton("Back", onClick = { handleBack() })
        }

        if (showPhotoDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoDialog = false },
                title = { Text("Update Profile Picture") },
                text = { Text("Choose an option") },
                confirmButton = {
                    TextButton(onClick = { showPhotoDialog = false; launchCamera() }) { Text("Take Photo") }
                },
                dismissButton = {
                    TextButton(onClick = { showPhotoDialog = false; galleryLauncher.launch("image/*") }) { Text("Choose from Gallery") }
                }
            )
        }
    }
}