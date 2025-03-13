package com.xyz.healthease

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        Log.d("NotificationReceiver", "Received Intent: ${intent.action}")
        val action = intent.action ?: return
        val patientId = intent.getStringExtra("patient_id") ?: return
        val doctorId = intent.getStringExtra("doctor_id")
        val familyId = intent.getStringExtra("family_id")
        val hospitalId = intent.getStringExtra("hospital_id")

        Log.d("NotificationReceiver", "Received Action: $action")
        Log.d("NotificationReceiver", "Received Patient ID: $patientId")
        Log.d("NotificationReceiver", "Received Doctor ID: $doctorId")
        Log.d("NotificationReceiver", "Received Family ID: $familyId")
        Log.d("NotificationReceiver", "Hospital ID: $hospitalId")

        val isDoctorRequest = !doctorId.isNullOrEmpty()
        val isHospitalRequest = !hospitalId.isNullOrEmpty()

        sendResponseToServer(context, patientId, doctorId, hospitalId, familyId, action, isDoctorRequest, isHospitalRequest)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notificationId = intent.getIntExtra("notification_id", patientId.hashCode())
        notificationManager.cancel(notificationId)
    }

    private fun sendResponseToServer(
        context: Context,
        patientId: String,
        doctorId: String?,
        hospitalId: String?,
        familyId: String?,
        action: String?,
        isDoctorRequest: Boolean,
        isHospitalRequest: Boolean
    ){
        val responseValue = if (action == "ACCEPT_ACTION") "granted" else "revoked"

        Log.d("NotificationReceiver", "Sending response: patientId=$patientId, action=$action")
        val requestBody = when {
            isDoctorRequest -> ApiService.ResponseAccessDoctorRequest(patient_id = patientId, doctor_id = doctorId ?: "", response = responseValue)
            isHospitalRequest -> ApiService.ResponseAccessHospitalRequest(patient_id = patientId, hospital_id = hospitalId ?: "", response = responseValue)
            else -> ApiService.ResponseAccessRequest(patient_id = patientId, family_id = familyId ?: "", response = responseValue)
        }


        val apiService = ApiClient.getApiService()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = when {
                    isDoctorRequest -> apiService.respondAccessDoctor(requestBody as ApiService.ResponseAccessDoctorRequest)
                    isHospitalRequest -> apiService.respondAccessHospital(requestBody as ApiService.ResponseAccessHospitalRequest)
                    else -> apiService.respondAccess(requestBody as ApiService.ResponseAccessRequest)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(appContext, "Access $responseValue successfully!", Toast.LENGTH_SHORT).show()
                        Log.d("NotificationReceiver", "Access $responseValue successfully!")

                    } else {
                        Log.e("NotificationReceiver", "Error: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            context,
                            "Failed to update access. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Exception in API call: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "An error occurred. Please check your connection.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}