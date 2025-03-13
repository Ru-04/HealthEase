package com.xyz.healthease

import com.google.gson.annotations.SerializedName
import java.util.Date


data class Patient(
    val patientName: String,
    val dob: String,
    val gender: String,
    val email: String,
    val contactNo: String,
    val fcm_token: String?
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
    val year_of_experience: Int,
    val fcm_token: String
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
    val License_No: String,
    val Hospital_Name: String,
    val Address: String,
    val Contact_No: String,
    val Email: String,
    val Capacity: Int,
    val Date_Of_Establish: String,
    val Fcm_token: String
)
data class RegistrationResponse(
    val message: String,
    val patientId: String
)

data class AddFamilyRequest(
    val patient_id: String,
    val family_id: String,
    val relation: String
)