package com.example.anipet_capstone.screens

import androidx.compose.foundation.layout.*
// Removed unused Button/OutlinedButton imports
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
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
    onBack: () -> Unit
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
                    OutlinedTextField(
                        value = address,
                        onValueChange = {},
                        label = { Text("Registered Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
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
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number*") },
                        modifier = Modifier.fillMaxWidth()
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
                        OutlinedTextField(
                            value = zoomDetails,
                            onValueChange = { zoomDetails = it },
                            label = { Text("Zoom meeting details*") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Link, meeting ID, or Zoom email") }
                        )
                    }
                    if (profileLoadError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(profileLoadError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date*") }, modifier = Modifier.weight(1f), placeholder = { Text("YYYY-MM-DD") })
                        OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Occupation") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company/Business Name*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = socialProfile, onValueChange = { socialProfile = it }, label = { Text("Social Media Profile") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Single, Married, Other") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = pronouns, onValueChange = { pronouns = it }, label = { Text("Pronouns*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("She/her, He/him, They/them") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = promptedBy, onValueChange = { promptedBy = it }, label = { Text("What prompted you to adopt?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Friends, Website, Social Media, Other") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = adoptedBefore, onValueChange = { adoptedBefore = it }, label = { Text("Have you adopted before?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yes or No") })
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

                StandardCard(title = "Alternate Contact") {
                    if (isMinorApplicant) {
                        Text(
                            "You are under 18 — an alternate contact is required.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(value = altName, onValueChange = { altName = it }, label = { Text("Name$altSuffix") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = altRelation, onValueChange = { altRelation = it }, label = { Text("Relationship$altSuffix") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = altPhone, onValueChange = { altPhone = it }, label = { Text("Phone$altSuffix") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = altEmail, onValueChange = { altEmail = it }, label = { Text("Email$altSuffix") }, modifier = Modifier.weight(1f))
                    }
                }

                StandardCard(title = "Questionnaire") {
                    OutlinedTextField(value = lookingFor, onValueChange = { lookingFor = it }, label = { Text("What are you looking to adopt?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Cat, Dog, Both, Not decided") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = specificAnimal, onValueChange = { specificAnimal = it }, label = { Text("Applying for a specific shelter animal?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yes or No") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = idealPet, onValueChange = { idealPet = it }, label = { Text("Describe your ideal pet*") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = buildingType, onValueChange = { buildingType = it }, label = { Text("Building type*") }, modifier = Modifier.weight(1f), placeholder = { Text("House, Apartment, Condo, Other") })
                        OutlinedTextField(value = doRent, onValueChange = { doRent = it }, label = { Text("Do you rent?*") }, modifier = Modifier.weight(1f), placeholder = { Text("Yes or No") })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = movePlan, onValueChange = { movePlan = it }, label = { Text("What happens if/when you move?*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = household, onValueChange = { household = it }, label = { Text("Who do you live with?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Living alone, Spouse, Parents, etc.") })
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = allergic, onValueChange = { allergic = it }, label = { Text("Any household allergies?*") }, modifier = Modifier.weight(1f), placeholder = { Text("Yes or No") })
                        OutlinedTextField(value = dailyCaregiver, onValueChange = { dailyCaregiver = it }, label = { Text("Who will care for pet?*") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = financialResponsible, onValueChange = { financialResponsible = it }, label = { Text("Who is financially responsible?*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = petSitter, onValueChange = { petSitter = it }, label = { Text("Who will look after your pet?*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = hoursLeft, onValueChange = { hoursLeft = it }, label = { Text("Hours left alone*") }, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = familySupport, onValueChange = { familySupport = it }, label = { Text("Does family support?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yes or No") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = familyExplain, onValueChange = { familyExplain = it }, label = { Text("Please explain") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = otherPets, onValueChange = { otherPets = it }, label = { Text("Do you have other pets?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yes or No") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = pastPets, onValueChange = { pastPets = it }, label = { Text("Have you had pets in the past?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yes or No") })
                }

                StandardCard(title = "Interview & Visitation") {
                    if (interactionMethod == "Zoom") {
                        OutlinedTextField(value = preferredDate, onValueChange = { preferredDate = it }, label = { Text("Preferred Zoom date*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("YYYY-MM-DD") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = preferredTime, onValueChange = { preferredTime = it }, label = { Text("Preferred Zoom time*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("HH:MM AM/PM") })
                    } else {
                        Text(
                            text = if (interactionMethod == "Phone") "We will use your registered phone number to contact you for the interview."
                            else "Select Zoom as your preferred interaction method to schedule a virtual meeting.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = willVisit, onValueChange = { willVisit = it }, label = { Text("Will you visit the shelter?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yes or No") })
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

                PrimaryButton(
                    text = "Submit Application",
                    onClick = {
                        if (petId.isBlank() || userId.isBlank() || applicantName.isBlank()) {
                            statusText = "Missing pet ID, user ID, or applicant name"
                            return@PrimaryButton
                        }
                        if (!termsAccepted) {
                            statusText = "Please accept the terms and privacy consent before submitting."
                            return@PrimaryButton
                        }

                        scope.launch {
                            try {
                                // Validate required form fields before submission
                                if (address.isBlank() || phone.isBlank() || email.isBlank() || birthDate.isBlank() || company.isBlank() || status.isBlank() || pronouns.isBlank() || promptedBy.isBlank() || adoptedBefore.isBlank() || interactionMethod.isBlank() || lookingFor.isBlank() || specificAnimal.isBlank() || idealPet.isBlank() || buildingType.isBlank() || doRent.isBlank() || movePlan.isBlank() || household.isBlank() || allergic.isBlank() || dailyCaregiver.isBlank() || financialResponsible.isBlank() || petSitter.isBlank() || hoursLeft.isBlank() || familySupport.isBlank() || otherPets.isBlank() || pastPets.isBlank() || willVisit.isBlank()) {
                                    statusText = "Please complete all required fields before submitting."
                                    return@launch
                                }
                                if (interactionMethod == "Zoom") {
                                    if (zoomDetails.isBlank()) {
                                        statusText = "Please provide Zoom meeting details for Zoom interaction."
                                        return@launch
                                    }
                                    if (preferredDate.isBlank() || preferredTime.isBlank()) {
                                        statusText = "Please complete both Zoom date and time for Zoom interaction."
                                        return@launch
                                    }
                                }

                                val applicantAge: Int? = try {
                                    val parts = birthDate.split("-")
                                    if (parts.size == 3) {
                                        val dob = java.util.Calendar.getInstance().apply {
                                            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                                        }
                                        val today = java.util.Calendar.getInstance()
                                        var age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR)
                                        if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) age--
                                        age
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                                if (applicantAge != null && applicantAge < 18) {
                                    if (altName.isBlank() || altRelation.isBlank() || altPhone.isBlank() || altEmail.isBlank()) {
                                        statusText = "You are under 18 — please provide an alternate contact's name, relationship, phone and email."
                                        return@launch
                                    }
                                }

                                if (idUri == null) {
                                    statusText = "Please attach a valid ID document."
                                    return@launch
                                }
                                if (houseUris.isEmpty()) {
                                    statusText = "Please attach at least one house photo."
                                    return@launch
                                }

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
                                if (!statusText.contains("Error") && statusText.isNotBlank()) {
                                    if (statusText.isNotBlank()) statusText += "\nCheck My Applications for updates."
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
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }

                SecondaryButton("Attach ID Document", onClick = { idPicker.launch("image/*") })
                SecondaryButton("Attach House Photos (multiple)", onClick = { housePicker.launch("image/*") })

                SecondaryButton("Cancel", onClick = onBack)

                if (statusText.isNotBlank()) {
                    StandardCard {
                        Text(statusText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }