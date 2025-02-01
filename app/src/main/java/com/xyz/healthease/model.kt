package com.xyz.healthease

import com.google.gson.annotations.SerializedName
import java.util.Date


data class Patient(
    val patientName: String,
    val dob: String,
    val gender: String,
    val email: String,
    val contactNo: String,
    // Add any additional fields here (e.g., address, medical history)
)

data class Doctor(
    val doctorName: String,
    val email: String,
    val contactNo: String,
    val gender: String,
    val dob: String,
    val qualification: String,
    val specialization: String,
    val affiliatedInstitutions: String,
    val year_of_experience: Int
)
// Request body for OTP generation
data class PhoneRequest(
    val phone: String // The phone number for OTP generation
)

// Request body for OTP verification
data class OtpRequest(
    val phone: String, // The phone number used for verification
    val otp: String    // The OTP entered by the user
)

// General API response structure
data class ApiResponse(
    val success: Boolean, // Indicates if the request was successful
    val message: String   // Provides a message about the operation result
)
data class DoctorS(
    val Doctor_Name: String,
    val Specialization: String)

data class Hospital(
    @SerializedName("Hospital_ID") val hospitalId: String, // ObjectId in MongoDB
    @SerializedName("Admin_ID") val adminId: String?, // ObjectId in MongoDB
    @SerializedName("License_No") val licenseNo: String,
    @SerializedName("Hospital_Name") val hospitalName: String,
    @SerializedName("Certification") val certification: String,
    @SerializedName("Address") val address: String,
    @SerializedName("Contact_No") val contactNo: String,
    @SerializedName("Email") val email: String,
    @SerializedName("Capacity") val capacity: Int,
    @SerializedName("Date_Of_Establish") val dateOfEstablish: String,
    @SerializedName("Medical_Services") val medicalServices: List<String> = emptyList(),
//    @SerializedName("Departments") val departments: List<Department> = emptyList(),
//    @SerializedName("permissions") val permissions: List<Permission> = emptyList(),
//    @SerializedName("audit_logs") val auditLogs: List<AuditLog> = emptyList()
)
data class RegistrationResponse(
    val message: String,
    val patientId: String
)


