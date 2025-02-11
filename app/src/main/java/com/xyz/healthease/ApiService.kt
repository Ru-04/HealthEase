package com.xyz.healthease

//import com.google.android.gms.common.api.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    // Endpoint for generating OTP
    @POST("/sendOtp")
    fun sendOtp(@Body request: PhoneRequest): Call<ApiResponse>

    // Endpoint for verifying OTP (assuming a new backend API for OTP verification)
    @POST("/verifyOtp")
    fun verifyOtp(@Body request: OtpRequest): Call<ApiResponse>

    // Existing endpoints
    @POST("/api/patient")
    suspend fun savePatient(@Body patient: Patient): RegistrationResponse

    @POST("/updateLoginStatus")
    suspend fun updateLoginStatus(@Body request: LogoutRequest): Response<LogoutResponse>

    @POST("/api/doctor")
    suspend fun saveDoctor(@Body doctor: Doctor): Map<String, String>


    @Multipart
    @POST("/api/report")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("patientId") patientId: RequestBody
    ): Map<String, Any>

    @GET("/api/doctor/search")
    fun getDoctors(): Call<List<DoctorS>>

    @POST("hospital/register")
    suspend fun saveHospital(@Body hospital: Hospital): Map<String, String>

    @Headers("Content-Type: application/json")
    @POST("/familyMember")
    fun familyMember(@Body request: AddFamilyRequest): Call<Map<String, Any>>


    // Request and response data classes for OTP-related calls
    data class PhoneRequest(val phone: String) // To request OTP
    data class OtpRequest(val phone: String, val otp: String) // To verify OTP
    data class ApiResponse(val message: String, val success: Boolean, val patientId: String?)
    data class LogoutRequest(
        val patient_id: String,
        val isLoggedIn: Boolean
    )
    data class LogoutResponse(
        val message: String? // Matches API response `{ "message": "Login status updated successfully" }`
    )
}
