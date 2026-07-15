package com.example.anipet_capstone.screens

import androidx.compose.foundation.layout.*
// Removed unused Button/OutlinedButton imports
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import java.util.Locale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

// session helpers moved to AppSession.kt

@Composable
fun ApplyAdoptionScreen(
    petId: String,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val applicantName = getSavedFullName(context) ?: ""
    val userId = getSavedUserId(context) ?: ""

    var message by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var socialProfile by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var pronouns by remember { mutableStateOf("") }
    var contactPreference by remember { mutableStateOf("Email") }
    var promptedBy by remember { mutableStateOf("") }
    var adoptedBefore by remember { mutableStateOf("") }
    var interactionMethod by remember { mutableStateOf("Email") }
    var interactionMenuExpanded by remember { mutableStateOf(false) }
    val interactionOptions = listOf("Email", "Phone", "Zoom")
    var zoomDetails by remember { mutableStateOf("") }
    var profileLoadError by remember { mutableStateOf("") }

    // True after the first failed submit attempt, turns on the red per-field indicators.
    var showErrors by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            try {
                val profileResponse = ApiClient.api.getUserProfile(userId)
                profileResponse.user?.let { user ->
                    if (address.isBlank()) address = user.address.orEmpty()
                    if (phone.isBlank()) phone = user.phone.orEmpty()
                    if (email.isBlank()) email = user.email
                    if (!user.contact_preference.isNullOrBlank()) {
                        contactPreference = user.contact_preference.replaceFirstChar { it.uppercase() }
                    }
                }
            } catch (e: Exception) {
                profileLoadError = "Unable to load saved profile: ${e.message}"
            }
        }
    }

    var altName by remember { mutableStateOf("") }
    var altRelation by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var altEmail by remember { mutableStateOf("") }

    var lookingFor by remember { mutableStateOf("") }
    var specificAnimal by remember { mutableStateOf("") }
    var idealPet by remember { mutableStateOf("") }
    var buildingType by remember { mutableStateOf("") }
    var doRent by remember { mutableStateOf("") }
    var movePlan by remember { mutableStateOf("") }
    var household by remember { mutableStateOf("") }
    var allergic by remember { mutableStateOf("") }
    var dailyCaregiver by remember { mutableStateOf("") }
    var financialResponsible by remember { mutableStateOf("") }
    var petSitter by remember { mutableStateOf("") }
    var hoursLeft by remember { mutableStateOf("") }
    var familySupport by remember { mutableStateOf("") }
    var familyExplain by remember { mutableStateOf("") }
    var otherPets by remember { mutableStateOf("") }
    var pastPets by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("") }
    var preferredTime by remember { mutableStateOf("") }
    var willVisit by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var idUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var houseUris by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    var termsAccepted by remember { mutableStateOf(false) }
    var returnPenaltyAmount by remember { mutableStateOf("1000.00") }

    LaunchedEffect(Unit) {
        try {
            val policyResponse = ApiClient.api.getReturnPolicy()
            policyResponse.policy?.computed_penalty?.let { amount ->
                returnPenaltyAmount = String.format(Locale.getDefault(), "%.2f", amount)
            }
        } catch (e: Exception) {
            // Keep the fallback amount; the terms text still discloses a penalty applies.
        }
    }

    val isMinorApplicant = remember(birthDate) {
        try {
            val parts = birthDate.split("-")
            if (parts.size == 3) {
                val dob = java.util.Calendar.getInstance().apply {
                    set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
                val today = java.util.Calendar.getInstance()
                var age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR)
                if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) age--
                age < 18
            } else false
        } catch (e: Exception) {
            false
        }
    }
    val altSuffix = if (isMinorApplicant) "*" else ""

    AppContainer {
        val contentModifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .padding(horizontal = 12.dp)

        Column(modifier = contentModifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTopBar("Apply for Adoption", onBack = onBack)

                StandardCard(title = "Application Details") {
                    InfoText("Pet ID", petId)
                    InfoText("User ID", userId)
                    InfoText("Applicant", applicantName)
                }

                StandardCard(title = "Applicant's Info") {
                    OutlinedTextField(value = applicantName, onValueChange = {}, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address*",
                        modifier = Modifier.fillMaxWidth(),
                        showError = showErrors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Registered Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Preferred contact method for this application",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = contactPreference == "Email", onClick = { contactPreference = "Email" })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = contactPreference == "Phone", onClick = { contactPreference = "Phone" })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Phone", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number*",
                        modifier = Modifier.fillMaxWidth(),
                        showError = showErrors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = interactionMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preferred Interaction*") },
                            trailingIcon = {
                                IconButton(onClick = { interactionMenuExpanded = !interactionMenuExpanded }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "toggle")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = interactionMenuExpanded,
                            onDismissRequest = { interactionMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            interactionOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        interactionMethod = option
                                        interactionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (interactionMethod == "Zoom") {
                        Spacer(modifier = Modifier.height(8.dp))
                        RequiredTextField(
                            value = zoomDetails,
                            onValueChange = { zoomDetails = it },
                            label = "Zoom meeting details*",
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "Link, meeting ID, or Zoom email",
                            showError = showErrors
                        )
                    }
                    if (profileLoadError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(profileLoadError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateField(value = birthDate, onValueChange = { birthDate = it }, label = "Birth Date*", modifier = Modifier.weight(1f), isError = showErrors && birthDate.isBlank())
                        OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Occupation") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = company, onValueChange = { company = it }, label = "Company/Business Name*", modifier = Modifier.fillMaxWidth(), showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = socialProfile, onValueChange = { socialProfile = it }, label = { Text("Social Media Profile") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = status, onValueChange = { status = it }, label = "Status*", modifier = Modifier.fillMaxWidth(), placeholder = "Single, Married, Other", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = pronouns, onValueChange = { pronouns = it }, label = "Pronouns*", modifier = Modifier.fillMaxWidth(), placeholder = "She/her, He/him, They/them", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = promptedBy, onValueChange = { promptedBy = it }, label = "What prompted you to adopt?*", modifier = Modifier.fillMaxWidth(), placeholder = "Friends, Website, Social Media, Other", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = adoptedBefore, onValueChange = { adoptedBefore = it }, label = "Have you adopted before?*", modifier = Modifier.fillMaxWidth(), placeholder = "Yes or No", showError = showErrors)
                }

                StandardCard(title = "Alternate Contact") {
                    if (isMinorApplicant) {
                        Text(
                            "You are under 18 — an alternate contact is required.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    RequiredTextField(value = altName, onValueChange = { altName = it }, label = "Name$altSuffix", modifier = Modifier.fillMaxWidth(), showError = showErrors && isMinorApplicant)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = altRelation, onValueChange = { altRelation = it }, label = "Relationship$altSuffix", modifier = Modifier.fillMaxWidth(), showError = showErrors && isMinorApplicant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RequiredTextField(value = altPhone, onValueChange = { altPhone = it }, label = "Phone$altSuffix", modifier = Modifier.weight(1f), showError = showErrors && isMinorApplicant)
                        RequiredTextField(value = altEmail, onValueChange = { altEmail = it }, label = "Email$altSuffix", modifier = Modifier.weight(1f), showError = showErrors && isMinorApplicant)
                    }
                }

                StandardCard(title = "Questionnaire") {
                    RequiredTextField(value = lookingFor, onValueChange = { lookingFor = it }, label = "What are you looking to adopt?*", modifier = Modifier.fillMaxWidth(), placeholder = "Cat, Dog, Both, Not decided", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = specificAnimal, onValueChange = { specificAnimal = it }, label = "Applying for a specific shelter animal?*", modifier = Modifier.fillMaxWidth(), placeholder = "Yes or No", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = idealPet, onValueChange = { idealPet = it }, label = "Describe your ideal pet*", modifier = Modifier.fillMaxWidth(), showError = showErrors, maxLines = 4)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RequiredTextField(value = buildingType, onValueChange = { buildingType = it }, label = "Building type*", modifier = Modifier.weight(1f), placeholder = "House, Apartment, Condo, Other", showError = showErrors)
                        RequiredTextField(value = doRent, onValueChange = { doRent = it }, label = "Do you rent?*", modifier = Modifier.weight(1f), placeholder = "Yes or No", showError = showErrors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = movePlan, onValueChange = { movePlan = it }, label = "What happens if/when you move?*", modifier = Modifier.fillMaxWidth(), showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = household, onValueChange = { household = it }, label = "Who do you live with?*", modifier = Modifier.fillMaxWidth(), placeholder = "Living alone, Spouse, Parents, etc.", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RequiredTextField(value = allergic, onValueChange = { allergic = it }, label = "Any household allergies?*", modifier = Modifier.weight(1f), placeholder = "Yes or No", showError = showErrors)
                        RequiredTextField(value = dailyCaregiver, onValueChange = { dailyCaregiver = it }, label = "Who will care for pet?*", modifier = Modifier.weight(1f), showError = showErrors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = financialResponsible, onValueChange = { financialResponsible = it }, label = "Who is financially responsible?*", modifier = Modifier.fillMaxWidth(), showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = petSitter, onValueChange = { petSitter = it }, label = "Who will look after your pet?*", modifier = Modifier.fillMaxWidth(), showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = hoursLeft, onValueChange = { hoursLeft = it }, label = "Hours left alone*", modifier = Modifier.fillMaxWidth(), showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = familySupport, onValueChange = { familySupport = it }, label = "Does family support?*", modifier = Modifier.fillMaxWidth(), placeholder = "Yes or No", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = familyExplain, onValueChange = { familyExplain = it }, label = { Text("Please explain") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = otherPets, onValueChange = { otherPets = it }, label = "Do you have other pets?*", modifier = Modifier.fillMaxWidth(), placeholder = "Yes or No", showError = showErrors)
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = pastPets, onValueChange = { pastPets = it }, label = "Have you had pets in the past?*", modifier = Modifier.fillMaxWidth(), placeholder = "Yes or No", showError = showErrors)
                }

                StandardCard(title = "Interview & Visitation") {
                    if (interactionMethod == "Zoom") {
                        DateField(value = preferredDate, onValueChange = { preferredDate = it }, label = "Preferred Zoom date*", modifier = Modifier.fillMaxWidth(), isError = showErrors && preferredDate.isBlank())
                        Spacer(modifier = Modifier.height(8.dp))
                        RequiredTextField(value = preferredTime, onValueChange = { preferredTime = it }, label = "Preferred Zoom time*", modifier = Modifier.fillMaxWidth(), placeholder = "HH:MM AM/PM", showError = showErrors)
                    } else {
                        Text(
                            text = if (interactionMethod == "Phone") "We will use your registered phone number to contact you for the interview."
                            else "Select Zoom as your preferred interaction method to schedule a virtual meeting.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RequiredTextField(value = willVisit, onValueChange = { willVisit = it }, label = "Will you visit the shelter?*", modifier = Modifier.fillMaxWidth(), placeholder = "Yes or No", showError = showErrors)
                }

                StandardCard(title = "Your Message") {
                    Text(
                        "Please explain why you would like to adopt this pet. Provide clear and honest details to help us assess your application.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppTextFieldColors()
                    )
                }

                val idMissing = showErrors && idUri == null
                val housePhotosMissing = showErrors && houseUris.isEmpty()
                val termsMissing = showErrors && !termsAccepted

                PrimaryButton(
                    text = "Submit Application",
                    onClick = {
                        if (petId.isBlank() || userId.isBlank() || applicantName.isBlank()) {
                            statusText = "Missing pet ID, user ID, or applicant name"
                            return@PrimaryButton
                        }

                        val missingRequired = address.isBlank() || phone.isBlank() || email.isBlank() || birthDate.isBlank() ||
                            company.isBlank() || status.isBlank() || pronouns.isBlank() || promptedBy.isBlank() ||
                            adoptedBefore.isBlank() || interactionMethod.isBlank() || lookingFor.isBlank() ||
                            specificAnimal.isBlank() || idealPet.isBlank() || buildingType.isBlank() || doRent.isBlank() ||
                            movePlan.isBlank() || household.isBlank() || allergic.isBlank() || dailyCaregiver.isBlank() ||
                            financialResponsible.isBlank() || petSitter.isBlank() || hoursLeft.isBlank() ||
                            familySupport.isBlank() || otherPets.isBlank() || pastPets.isBlank() || willVisit.isBlank() ||
                            (interactionMethod == "Zoom" && (zoomDetails.isBlank() || preferredDate.isBlank() || preferredTime.isBlank())) ||
                            (isMinorApplicant && (altName.isBlank() || altRelation.isBlank() || altPhone.isBlank() || altEmail.isBlank()))

                        if (missingRequired) {
                            showErrors = true
                            statusText = "Please complete the fields marked \"This field is required\" below."
                            return@PrimaryButton
                        }

                        if (idUri == null) {
                            showErrors = true
                            statusText = "Please attach a valid ID document."
                            return@PrimaryButton
                        }
                        if (houseUris.isEmpty()) {
                            showErrors = true
                            statusText = "Please attach at least one house photo."
                            return@PrimaryButton
                        }
                        if (!termsAccepted) {
                            showErrors = true
                            statusText = "Please accept the terms and privacy consent before submitting."
                            return@PrimaryButton
                        }

                        showErrors = false

                        scope.launch {
                            try {
                                val formObject = JSONObject().apply {
                                    put("address", address)
                                    put("phone", phone)
                                    put("email", email)
                                    put("birth_date", birthDate)
                                    put("occupation", occupation)
                                    put("company", company)
                                    put("social_profile", socialProfile)
                                    put("status", status)
                                    put("pronouns", pronouns)
                                    put("prompted_by", promptedBy)
                                    put("adopted_before", adoptedBefore)
                                    put("interaction_method", interactionMethod)
                                    put("contact_preference", contactPreference)
                                    put("zoom_details", zoomDetails)
                                    put("alt_name", altName)
                                    put("alt_relation", altRelation)
                                    put("alt_phone", altPhone)
                                    put("alt_email", altEmail)
                                    put("looking_for", lookingFor)
                                    put("specific_animal", specificAnimal)
                                    put("ideal_pet", idealPet)
                                    put("building_type", buildingType)
                                    put("do_rent", doRent)
                                    put("move_plan", movePlan)
                                    put("household", household)
                                    put("allergic", allergic)
                                    put("daily_caregiver", dailyCaregiver)
                                    put("financial_responsible", financialResponsible)
                                    put("pet_sitter", petSitter)
                                    put("hours_left", hoursLeft)
                                    put("family_support", familySupport)
                                    put("family_explain", familyExplain)
                                    put("other_pets", otherPets)
                                    put("past_pets", pastPets)
                                    put("preferred_date", preferredDate)
                                    put("preferred_time", preferredTime)
                                    put("will_visit", willVisit)
                                }
                                val formDataBody = formObject.toString()
                                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                                val resolver = context.contentResolver

                                val idPart: MultipartBody.Part? = idUri?.let { uri ->
                                    val input: InputStream? = resolver.openInputStream(uri)
                                    val bytes = input?.readBytes() ?: ByteArray(0)
                                    val mime = resolver.getType(uri) ?: "application/octet-stream"
                                    val rb: RequestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
                                    val filename = uri.lastPathSegment ?: "id_document"
                                    MultipartBody.Part.createFormData("id_document", filename, rb)
                                }

                                val houseParts = houseUris.map { uri ->
                                    val bytes = resolver.openInputStream(uri)?.readBytes() ?: ByteArray(0)
                                    val mime = resolver.getType(uri) ?: "image/jpeg"
                                    val rb: RequestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
                                    val filename = uri.lastPathSegment ?: "house_photo"
                                    MultipartBody.Part.createFormData("house_photos[]", filename, rb)
                                }

                                val res = ApiClient.api.applyAdoptionWithDocs(
                                    petId = petId,
                                    userId = userId,
                                    applicantName = applicantName,
                                    message = message,
                                    formData = formDataBody,
                                    termsAccepted = if (termsAccepted) "1" else "0",
                                    privacyConsent = "I agree to the terms and conditions and consent to the use of my private information solely for the adoption application process. I understand that if I return the adopted pet, a return penalty of ₱$returnPenaltyAmount applies, per shelter policy.",
                                    idDocument = idPart,
                                    housePhotos = houseParts.ifEmpty { null }
                                )
                                statusText = res.message
                                if (res.status == "success") {
                                    showSuccessDialog = true
                                }
                            } catch (e: Exception) {
                                statusText = "Error: ${e.message}"
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // File pickers
                val idPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
                    if (uri != null) idUri = uri
                }

                val housePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<android.net.Uri> ->
                    houseUris = uris
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "I agree to the terms and conditions and consent to the use of my private information solely for the adoption application process. I understand that if I return the adopted pet, a return penalty of ₱$returnPenaltyAmount applies, per shelter policy.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (termsMissing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }

                SecondaryButton("Attach ID Document" + if (idUri != null) " ✓" else "", onClick = { idPicker.launch("image/*") })
                if (idUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.size(140.dp)) {
                        AsyncImage(
                            model = idUri,
                            contentDescription = "Selected ID document",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { idUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(bottomStart = 10.dp, topEnd = 12.dp))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove ID document", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                if (idMissing) {
                    Text("This field is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                SecondaryButton("Attach House Photos (multiple)" + if (houseUris.isNotEmpty()) " ✓ (${houseUris.size})" else "", onClick = { housePicker.launch("image/*") })
                if (houseUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(houseUris) { uri ->
                            Box(modifier = Modifier.size(100.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected house photo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { houseUris = houseUris.filterNot { it == uri } },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(bottomStart = 8.dp, topEnd = 10.dp))
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove house photo", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
                if (housePhotosMissing) {
                    Text("This field is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                SecondaryButton("Cancel", onClick = onBack)

                if (statusText.isNotBlank() && !showSuccessDialog) {
                    StandardCard {
                        Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { /* require explicit acknowledgement */ },
                icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF1B998B)) },
                title = { Text("Application Submitted!") },
                text = { Text("Your adoption application was submitted successfully. Go to My Applications to check its status.") },
                confirmButton = {
                    PrimaryButton(
                        text = "OK",
                        onClick = {
                            showSuccessDialog = false
                            onSubmitSuccess()
                        }
                    )
                }
            )
        }
    }
