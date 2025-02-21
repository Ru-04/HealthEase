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

        val action = intent.action ?: return
       // val notificationId = intent.getStringExtra("notificationId") ?: return
        val patientId = intent.getStringExtra("patient_id") ?: return
        val familyId = intent.getStringExtra("family_id") ?: return
        Log.d("NotificationReceiver", "Received Action: $action")
        Log.d("NotificationReceiver", "Received Patient ID: $patientId")
        Log.d("NotificationReceiver", "Received Family ID: $familyId")

        when (action) {
            "ACCEPT_ACTION" -> Log.d("NotificationReceiver", "Accept clicked for patientId: $patientId, familyId: $familyId")
            "REJECT_ACTION" -> Log.d("NotificationReceiver", "Reject clicked for patientId: $patientId, familyId: $familyId")
        }

        sendResponseToServer(context, patientId, familyId, action)

        // Dismiss the notification after action is performed
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(patientId.hashCode()) // Unique notification ID based on patientId
    }

    private fun sendResponseToServer(context: Context, patientId: String, familyId: String, action: String?) {
        val responseValue = if (action == "ACCEPT_ACTION") "granted" else "revoked"
        Log.d("NotificationReceiver", "Sending response to server: patientId=$patientId, familyId=$familyId, response=$responseValue")
        // ✅ Correctly creating the request object
        val requestBody = ApiService.ResponseAccessRequest(
            patient_id = patientId,
            family_id = familyId,
            response = responseValue
        )

        val apiService = ApiClient.getApiService()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.respondAccess(requestBody)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Access $responseValue successfully!", Toast.LENGTH_SHORT).show()
                        Log.d("NotificationReceiver", "Access $responseValue successfully!")

                        if (responseValue == "granted") {
                            fetchFamilyReports(context, patientId, familyId)
                        } else {
                            Log.d("NotificationReceiver", "Access was revoked, no reports to fetch.")
                        }
                    } else {
                        Log.e("NotificationReceiver", "Error: ${response.errorBody()?.string()}")
                        Toast.makeText(context, "Failed to update access. Please try again.", Toast.LENGTH_LONG).show()
                    }

                }
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Exception in API call: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "An error occurred. Please check your connection.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun fetchFamilyReports(context: Context, patientId: String, familyId: String) {
        Log.d("NotificationReceiver", "Fetching reports for patientId=$patientId, familyId=$familyId")

        if (patientId.isBlank() || familyId.isBlank()) {
            Log.e("NotificationReceiver", "Missing patient_id or family_id in intent")
            return
        }else{
            print("no error with id")
        }

        val requestBody = ApiService.GetFamilyReportsRequest(
            patient_id = patientId,  // Ensure correct property name
            family_id = familyId
        )

        if (patientId.isNullOrEmpty() || familyId.isNullOrEmpty()) {
            Log.e("NotificationReceiver", "Missing patient_id or family_id in intent")
            return }
        val apiService = ApiClient.getApiService()
        CoroutineScope(Dispatchers.IO).launch {
            try {


                val response = apiService.getFamilyReports(requestBody)
                Log.d("NotificationReceiver", "Sending Request Body: $requestBody")


                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val reports = response.body()?.images

                        if (!reports.isNullOrEmpty()) {
                            Toast.makeText(context, "Files received: ${reports.size}", Toast.LENGTH_LONG).show()
                            Log.d("NotificationReceiver", "Files received: $reports")
                        } else {
                            Log.d("NotificationReceiver", "No files received.")
                        }
                    } else {
                        Log.e("NotificationReceiver", "Failed to fetch reports: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(context, "Failed to fetch reports. Try again later.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Exception in fetching reports: ${e.message}")
            }
        }
    }
}

