package com.xyz.healthease

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST



interface ApiService {
    @POST("/sendOtp")
    fun sendOtp(@Body request: PhoneRequest): Call<ApiResponse>

    // Endpoint for verifying OTP (assuming a new backend API for OTP verification)
    @POST("/verifyOtp")
    fun verifyOtp(@Body request: OtpRequest): Call<ApiResponse>

    // Existing endpoints
    @POST("/api/patient")
    suspend fun savePatient(@Body patient: Patient): RegistrationResponse

    @POST("/updateLoginStatus")
    suspend fun updateLoginStatus(@Body request: PatientLogoutRequest): Response<LogoutResponse>

    @POST("/api/doctor")
    suspend fun saveDoctor(@Body doctor: Doctor): Map<String, String>


    @POST("/api/report")
    suspend fun uploadReport(@Body reportData: Map<String, String>): Map<String, Any>


    @GET("/api/doctor/search")
    fun getDoctors(): Call<List<DoctorS>>

    @POST("/api/hospital")
    suspend fun saveHospital(@Body hospital: Hospital): HospitalResponse

    @Headers("Content-Type: application/json")
    @POST("/familyMember")
    fun familyMember(@Body request: AddFamilyRequest): Call<Map<String, Any>>

 //   @POST("/respondAccess")
   // suspend fun respondAccess(@Body request: ResponseAccessRequest): Response<ResponseBody>

    @POST("/getFamilyReports")
    suspend fun getFamilyReports(
        @Body request: GetFamilyReportsRequest
    ): Response<GetReportsResponse>

    @POST("/api/add-child") // Replace with your actual endpoint
    fun addChild(@Body child: ChildRequest): Call<ChildResponse>

    @POST("/updateLoginStatusDoctor") // Ensure this matches your backend endpoint
    suspend fun updateLoginStatusD(@Body request: DoctorLogoutRequest): Response<LogoutResponse>

    data class PatientLogoutRequest(
        val patient_id: String,
        val isLoggedIn: Boolean
    )

    data class DoctorLogoutRequest(
        val doctor_id: String,
        val isLoggedIn: Boolean
    )

    data class LogoutResponse(
        val message: String
    )

    data class ChildRequest(
        val patientId: String,
        val name: String,
        val age: Int,
        val parentAccess: Boolean,
        val relation: String
    )

    data class ChildResponse(
        val message: String,
        val childId: String?
    )

    data class GetFamilyReportsRequest(
        val patient_id: String,
        val family_id: String,
    )

    data class Report(
        val public_id: String,
        val id: String,
        val images: List<String>,
        val report_category: String
    )

    data class GetReportsResponse(
        val reports: List<Report>
    )

    data class PhoneRequest(val phone: String , val role: String?) // To request OTP
    data class OtpRequest(val phone: String, val otp: String, val role: String) // To verify OTP
    data class ApiResponse(val message: String, val success: Boolean, val userId: String?)




    @POST("/updateLoginStatus")
    suspend fun updateLoginStatus(@Body request: LogoutRequest): Response<LogoutResponse>


    @POST("/getPatientReports")  // Matches the Node.js route
    suspend fun getPatientReports(@Body request: GetPatientReportsRequest): Response<GetReportsResponse>


   @POST("getfamilyname")
    @Headers("Content-Type: application/json")
    suspend fun getFamilyName(@Body requestBody: FamilyIdRequest): Response<FamilyMember>


    data class LogoutRequest(
        val patient_id: String,
        val isLoggedIn: Boolean
    )


    data class GetPatientReportsRequest(
        val patient_id: String
    )


    data class FamilyIdRequest(
        val family_id: String
    )
    data class FamilyMember(
        val family_id: String,
        val family_name: String
    )


        // Upload child report
        @POST("/api/childReport")
        suspend fun uploadChildReport(@Body request: ChildReportRequest): Response<ChildReportResponse>

        // Fetch child reports
        @POST("/getPatientChildReports")
        suspend fun getPatientChildReports(@Body request: FetchChildReportsRequest): Response<FetchChildReportsResponse>

        // Data class for uploading child report
        data class ChildReportRequest(
            val patientId: String,
            val childId: String,
            val publicId: String,
            val imageUrl: String,
            val reportCategory: String,
            val hospitalName: String,
            val doctorName: String
        )

        // Response for uploading a child report
        data class ChildReportResponse(
            val message: String
        )

    // Request to fetch child reports
    data class FetchChildReportsRequest(
        val patientId: String,  // Ensure consistency with backend
        val childId: String
    )
    // Response when fetching child reports
    data class FetchChildReportsResponse(
        val childId: String,
        val reports: List<ChildReport>
    )

    // Child Report Data Model (matches MongoDB format)
    data class ChildReport(
        val publicId: String,
        val reportCategory: String,
        val images: List<String> // List of image URLs
    )

/////////////////////////////////////////////////////////////////

    @POST("doctorMember")
    fun sendMemberDetails(@Body request: MemberRequest): Call<Void>

    @POST("/respondAccess")
    suspend fun respondAccess(@Body request: ResponseAccessRequest): Response<ResponseBody>

    @POST("/respondAccessdoctor")
    suspend fun respondAccessDoctor(@Body request: ResponseAccessDoctorRequest): Response<ResponseBody>

    @POST("/hospitalMember")
    fun sendHospitalMemberDetails(@Body request: MemberRequest2): Call<Void>

    data class HospitalLogoutRequest(
        val hospital_id: String,
        val isLoggedIn: Boolean
    )

    data class HospitalLogoutResponse(
        val message: String
    )

    @POST("/updateLoginStatusHospital")
    suspend fun updateLoginStatus(@Body request: HospitalLogoutRequest): Response<HospitalLogoutResponse>

    @POST("/respondAccesshospital")
    suspend fun respondAccessHospital(@Body request: ResponseAccessHospitalRequest): Response<ApiResponse2>

    data class ResponseAccessHospitalRequest(
        val patient_id: String,
        val hospital_id: String,
        val response: String
    )

    data class ApiResponse2(
        val success: Boolean,
        val message: String
    )
    data class MemberRequest2(
        val patient_id: String,
        val hospital_id: String
    )

    data class HospitalResponse(
        val message: String,
        val hospitalId: String
    )
    data class ResponseAccessRequest(
        val patient_id: String,
        val family_id: String,
        val response: String
    )

    data class ResponseAccessDoctorRequest(
        val patient_id: String,
        val doctor_id: String,
        val response: String
    )


    data class MemberRequest(
        val patient_id: String,
        val doctor_id: String
    )

    //api and data class for list of child
    @POST("/getChildrenList")
    fun getChildrenList(@Body request: PatientIdRequest): Call<ChildrenResponse>

    data class PatientIdRequest(
        val patientId: String
    )


    data class ChildrenResponse(
        val children: List<Child>
    )

    @Parcelize
    data class Child(
        val child_id: String,
        val name: String
    ) : Parcelable

    //api and data class for displaying list of those id whose report can be viewed by patient

    @POST("/getFamilyList")
    fun getFamilyMembers(@Body request: PatientRequest): Call<FamilyResponse>

    data class PatientRequest(val patient_id: String)

    data class FamilyMember2(
        @SerializedName("family_member_id") val memberId: String,
        @SerializedName("name") val memberName: String
    )

    data class FamilyResponse(
        @SerializedName("patient_id") val patientId: String,
        @SerializedName("family_members") val familyMembers: List<FamilyMember2>
    )

    // api for sending patient id and family id to mongoDb to ultimately fetch
// and display report of those patient who gave access to view its report


    @POST("/getFamilyReports")
    fun getFamilyReports(@Body request: ReportRequest): Call<ReportResponse>

    data class ReportRequest(
        val patient_id: String,
        val family_id: String
    )

    data class ReportItem(
        val publicId: String,
        val reportCategory: String,
        val images: List<String>
    )

    data class ReportResponse(
        val reports: List<ReportItem>
    )
    // for displaying patient report by doctor

    @POST("/getReportsdoctor")
    suspend fun getReportsDoctor(
        @Body request: DoctorAccessRequest
    ): Response<List<Reportfordoctor>>

    data class DoctorAccessRequest(
        val patient_id: String,
        val doctor_id: String
    )

    // Report model
    data class Reportfordoctor(
        val publicId: String,
        val imageUrl: String,
        val reportCategory: String
    )

}