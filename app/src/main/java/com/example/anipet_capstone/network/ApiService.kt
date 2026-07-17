package com.example.anipet_capstone.network

import com.example.anipet_capstone.models.ApplicationsResponse
import com.example.anipet_capstone.models.ApplyResponse
import com.example.anipet_capstone.models.AuthResponse
import com.example.anipet_capstone.models.PetDetailResponse
import com.example.anipet_capstone.models.PetsResponse
import com.example.anipet_capstone.models.UserProfileResponse
import com.example.anipet_capstone.models.AppointmentsResponse
import com.example.anipet_capstone.models.AppointmentResponse
import com.example.anipet_capstone.models.ApplicationStatusResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import com.example.anipet_capstone.models.QrVerifyResponse
import com.example.anipet_capstone.models.OtpResponse
import com.example.anipet_capstone.models.ReturnPolicyResponse
import com.example.anipet_capstone.models.DonationResponse


interface ApiService {

    @GET("verify_qr.php")
    suspend fun verifyQr(
        @Query("qr_code") qrCode: String
    ): QrVerifyResponse

    @FormUrlEncoded
    @POST("register.php")
    suspend fun registerUser(
        @Field("username") username: String,
        @Field("first_name") firstName: String,
        @Field("middle_name") middleName: String,
        @Field("last_name") lastName: String,
        @Field("suffix") suffix: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("address") address: String,
        @Field("contact_preference") contactPreference: String,
        @Field("password") password: String,
        @Field("confirm_password") confirmPassword: String
    ): AuthResponse

    @FormUrlEncoded
    @POST("send_otp.php")
    suspend fun sendOtp(
        @Field("email") email: String
    ): OtpResponse

    @FormUrlEncoded
    @POST("verify_otp.php")
    suspend fun verifyOtp(
        @Field("email") email: String,
        @Field("otp") otp: String
    ): OtpResponse

    @FormUrlEncoded
    @POST("login.php")
    suspend fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): AuthResponse

    @GET("get_pet.php")
    suspend fun getPet(
        @Query("pet_id") petId: String
    ): PetDetailResponse

    @GET("get_applications.php")
    suspend fun getApplications(
        @Query("user_id") userId: String
    ): ApplicationsResponse

    @GET("get_user_profile.php")
    suspend fun getUserProfile(
        @Query("user_id") userId: String
    ): UserProfileResponse

    @FormUrlEncoded
    @POST("update_user_profile.php")
    suspend fun updateUserProfile(
        @Field("user_id") userId: String,
        @Field("full_name") fullName: String,
        @Field("phone") phone: String,
        @Field("address") address: String,
        @Field("contact_preference") contactPreference: String
    ): UserProfileResponse

    @GET("get_appointments.php")
    suspend fun getAppointments(
        @Query("user_id") userId: String
    ): AppointmentsResponse

    @FormUrlEncoded
    @POST("book_appointment.php")
    suspend fun bookAppointment(
        @Field("user_id") userId: String,
        @Field("title") title: String,
        @Field("details") details: String,
        @Field("scheduled_at") scheduledAt: String,
        @Field("pet_id") petId: String?
    ): AppointmentResponse

    @GET("update_application_status.php")
    suspend fun getApplicationStatus(
        @Query("application_id") applicationId: String
    ): ApplicationStatusResponse

    @GET("get_pets.php")
    suspend fun getPets(): PetsResponse

    @FormUrlEncoded
    @POST("apply_adoption.php")
    suspend fun applyAdoption(
        @Field("pet_id") petId: String,
        @Field("user_id") userId: String,
        @Field("applicant_name") applicantName: String,
        @Field("message") message: String
    ): ApplyResponse

    @Multipart
    @POST("apply_adoption.php")
    suspend fun applyAdoptionWithDocs(
        @Part("pet_id") petId: String,
        @Part("user_id") userId: String,
        @Part("applicant_name") applicantName: String,
        @Part("message") message: String,
        @Part("form_data") formData: RequestBody,
        @Part("terms_accepted") termsAccepted: String,
        @Part("privacy_consent") privacyConsent: String,
        @Part idDocument: MultipartBody.Part?,
        @Part housePhotos: List<MultipartBody.Part>?
    ): ApplyResponse

    @GET("get_return_policy.php")
    suspend fun getReturnPolicy(): ReturnPolicyResponse

    @Multipart
    @POST("submit_donation.php")
    suspend fun submitDonation(
        @Part("user_id") userId: RequestBody,
        @Part("donor_name") donorName: RequestBody,
        @Part("pet_name") petName: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("reference_number") referenceNumber: RequestBody,
        @Part("payment_method") paymentMethod: RequestBody,
        @Part receipt: MultipartBody.Part?
    ): DonationResponse

    @FormUrlEncoded
    @POST("request_return.php")
    suspend fun requestReturn(
        @Field("application_id") applicationId: String,
        @Field("user_id") userId: String,
        @Field("pet_id") petId: String,
        @Field("reason") reason: String,
        @Field("penalty_amount") penaltyAmount: String
    ): ApplyResponse

    @FormUrlEncoded
    @POST("request_interview.php")
    suspend fun requestInterview(
        @Field("application_id") applicationId: String
    ): ApplyResponse
}