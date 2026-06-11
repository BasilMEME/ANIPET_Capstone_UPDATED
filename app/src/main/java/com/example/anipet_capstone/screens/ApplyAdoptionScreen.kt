package com.example.anipet_capstone.screens

import android.content.Context
import androidx.compose.foundation.layout.*
// Removed unused Button/OutlinedButton imports
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.anipet_capstone.network.ApiClient
import kotlinx.coroutines.launch

fun getSavedFullName(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("full_name", null)
}

fun getSavedUserId(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("user_id", null)
}

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
    var promptedBy by remember { mutableStateOf("") }
    var adoptedBefore by remember { mutableStateOf("") }

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
    var introSteps by remember { mutableStateOf("") }
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
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone*") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email*") }, modifier = Modifier.weight(1f))
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

                StandardCard(title = "Alternate Contact") {
                    OutlinedTextField(value = altName, onValueChange = { altName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = altRelation, onValueChange = { altRelation = it }, label = { Text("Relationship") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = altPhone, onValueChange = { altPhone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = altEmail, onValueChange = { altEmail = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
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
                        OutlinedTextField(value = introSteps, onValueChange = { introSteps = it }, label = { Text("Introduce pet steps*") }, modifier = Modifier.weight(1f), maxLines = 3)
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
                    OutlinedTextField(value = preferredDate, onValueChange = { preferredDate = it }, label = { Text("Preferred Zoom date*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("YYYY-MM-DD") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = preferredTime, onValueChange = { preferredTime = it }, label = { Text("Preferred Zoom time*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("HH:MM AM/PM") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = willVisit, onValueChange = { willVisit = it }, label = { Text("Will you visit the shelter?*") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yes or No") })
                }

                StandardCard(title = "Your Message") {
                    Text("Tell us why you'd like to adopt this pet.", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            focusedBorderColor = Color.White.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                PrimaryButton(
                    text = "Submit Application",
                    onClick = {
                        if (petId.isBlank() || userId.isBlank() || applicantName.isBlank()) {
                            statusText = "Missing pet ID, user ID, or applicant name"
                            return@PrimaryButton
                        }

                        scope.launch {
                            try {
                                // Validate required form fields before submission
                                if (address.isBlank() || phone.isBlank() || email.isBlank() || birthDate.isBlank() || company.isBlank() || status.isBlank() || pronouns.isBlank() || promptedBy.isBlank() || adoptedBefore.isBlank() || lookingFor.isBlank() || specificAnimal.isBlank() || idealPet.isBlank() || buildingType.isBlank() || doRent.isBlank() || movePlan.isBlank() || household.isBlank() || allergic.isBlank() || dailyCaregiver.isBlank() || financialResponsible.isBlank() || petSitter.isBlank() || hoursLeft.isBlank() || introSteps.isBlank() || familySupport.isBlank() || otherPets.isBlank() || pastPets.isBlank() || preferredDate.isBlank() || preferredTime.isBlank() || willVisit.isBlank()) {
                                    statusText = "Please complete all required fields before submitting."
                                    return@launch
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
                                    put("intro_steps", introSteps)
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

                SecondaryButton("Attach ID Document", onClick = { idPicker.launch("image/*") })
                SecondaryButton("Attach House Photos (multiple)", onClick = { housePicker.launch("image/*") })

                SecondaryButton("Cancel", onClick = onBack)

                if (statusText.isNotBlank()) {
                    StandardCard {
                        Text(statusText, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }