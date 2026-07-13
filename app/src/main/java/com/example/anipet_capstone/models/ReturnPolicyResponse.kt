package com.example.anipet_capstone.models

data class ReturnPolicy(
    val penalty_type: String? = null,
    val penalty_amount: String? = null,
    val penalty_base_amount: String? = null,
    val computed_penalty: Double? = null,
    val dog_pound_name: String? = null,
    val dog_pound_contact: String? = null,
    val dog_pound_address: String? = null,
    val dog_pound_notes: String? = null,
    val donation_qr_filename: String? = null,
    val donation_gcash_name: String? = null,
    val donation_gcash_number: String? = null,
    val donation_notes: String? = null
)

data class ReturnPolicyResponse(
    val status: String,
    val policy: ReturnPolicy? = null
)
