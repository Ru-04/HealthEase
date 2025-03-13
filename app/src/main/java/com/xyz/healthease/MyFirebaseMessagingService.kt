package com.xyz.healthease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val CHANNEL_ID = "notification"

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val apiService: ApiService by lazy { ApiClient.getApiService() }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("MyFirebaseMessagingService", "FCM Message Received")

        val data = remoteMessage.data
        val patientId = data["patient_id"]
        val doctorId = data["doctor_id"]
        val hospitalId = data["hospital_id"]
        val actionType = data["actionType"]
        val status = data["status"]
        val doctorName = data["doctor_name"] ?: "Doctor"
        val hospitalName = data["hospital_name"] ?: "Hospital"
        val familyId = data["family_id"]
        val title = data["title"] ?: "HEALTHEASE"
        val message = data["message"] ?: "Test Message: Check Backend Payload"

        val notificationId = data["notificationId"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()

        Log.d("MyFirebaseMessagingService", "Patient ID: $patientId")
        Log.d("MyFirebaseMessagingService", "Doctor ID: $doctorId")
        Log.d("MyFirebaseMessagingService", "Hospital ID: $hospitalId")
        Log.d("MyFirebaseMessagingService", "Action Type: $actionType")
        Log.d("MyFirebaseMessagingService", "Status: $status")

        when {
            // ✅ Doctor requests access from a patient
            actionType == "doctor_request" && !doctorId.isNullOrEmpty() -> {
                createNotificationChannel()
                showCustomNotification(notificationId, "$doctorName wants access", "Allow access to your reports?", patientId, doctorId, isDoctor = true, isHospital = false)
            }

            // ✅ Hospital requests access from a patient
            actionType == "hospital_request" && !hospitalId.isNullOrEmpty() -> {
                createNotificationChannel()
                showCustomNotification(notificationId, "$hospitalName requests access", "Grant access to the hospital?", patientId, hospitalId, isDoctor = false, isHospital = true)
            }

            // ✅ Doctor receives access granted notification
            /*status == "granted" && !doctorId.isNullOrEmpty() && !patientId.isNullOrEmpty() -> {
                createNotificationChannel()
                showBasicNotification(notificationId, "Access Granted", "Patient granted your Doctor access.")
                val intent = Intent("ACCESS_GRANTED_DOCTOR")
                intent.putExtra("message", "Patient granted access.")
                sendBroadcast(intent)
            }*/

            // ✅ Doctor receives access granted → redirect to DoctorReportActivity
            status == "granted" && !doctorId.isNullOrEmpty() && !patientId.isNullOrEmpty() -> {
                createNotificationChannel()
                showBasicNotification(notificationId, "Access Granted", "Patient granted your Doctor access.")

                // ✅ Immediately launch DoctorReportActivity
                val intent = Intent(this, DoctorReportActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("patient_id", patientId)
                }
                startActivity(intent)
            }


            !patientId.isNullOrEmpty() && !familyId.isNullOrEmpty() && status == "granted"-> {
                createNotificationChannel()
                showBasicNotification(notificationId, "Access Granted", "Family granted your request for access.")

                // Send a broadcast to update the UI
                Log.d("MyFirebaseMessagingService", "Fetching family name for ID: $familyId")
                fetchFamilyName(familyId, patientId, "family")
                val intent = Intent("com.xyz.healthease.FAMILY_UPDATE")
                intent.putExtra("family_name", "New Family Member")
                intent.putExtra("family_id", familyId)
                sendBroadcast(intent)
            }


            // ✅ Hospital receives access granted notification
            status == "granted" && !hospitalId.isNullOrEmpty() && !patientId.isNullOrEmpty() -> {
                createNotificationChannel()
                showBasicNotification(notificationId, "Access Granted", "Patient granted your hospital access.")

                val intent = Intent("ACCESS_GRANTED_HOSPITAL")
                intent.putExtra("message", "Access granted successfully.")
                sendBroadcast(intent)
            }

            // ✅ Family member requests access
            actionType == "access_request" && !familyId.isNullOrEmpty() -> {
                createNotificationChannel()
                showCustomNotification(notificationId, title, message, patientId, familyId, isDoctor = false, isHospital = false)
            }
        }
    }

    private fun showCustomNotification(notificationId: Int, title: String, message: String, patientId: String?, requesterId: String?, isDoctor: Boolean, isHospital: Boolean) {
        val acceptIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "ACCEPT_ACTION"
            putExtra("notificationId", notificationId.toString())
            putExtra("patient_id", patientId ?: "UNKNOWN")
            putExtra(if (isDoctor) "doctor_id" else if (isHospital) "hospital_id" else "family_id", requesterId ?: "UNKNOWN")
        }

        val rejectIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "REJECT_ACTION"
            putExtra("notificationId", notificationId.toString())
            putExtra("patient_id", patientId ?: "UNKNOWN")
            putExtra(if (isDoctor) "doctor_id" else if (isHospital) "hospital_id" else "family_id", requesterId ?: "UNKNOWN")
        }

        val acceptPendingIntent = PendingIntent.getBroadcast(
            this, 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectPendingIntent = PendingIntent.getBroadcast(
            this, 2, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteViews = RemoteViews(packageName, R.layout.notification).apply {
            setTextViewText(R.id.title, title)
            setTextViewText(R.id.description, message)
            setOnClickPendingIntent(R.id.allow, acceptPendingIntent)
            setOnClickPendingIntent(R.id.deny, rejectPendingIntent)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun showBasicNotification(notificationId: Int, title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }


    private fun fetchFamilyName(familyId: String, patientId: String, relation: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getFamilyName(ApiService.FamilyIdRequest(familyId))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val familyMember = response.body()
                        if (familyMember != null) {
                            Log.d("MyFirebaseMessagingService", "Received Family Name: ${familyMember.family_name}, Family ID: ${familyMember.family_id}")

                            val intent = Intent("com.xyz.healthease.FAMILY_UPDATE")
                            intent.putExtra("family_name", familyMember.family_name)
                            intent.putExtra("family_id", familyMember.family_id)

                            val homeIntent = Intent(this@MyFirebaseMessagingService, homepage_patient::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(homeIntent)

                            val updateIntent = Intent("com.xyz.healthease.FAMILY_LIST_UPDATED")
                            LocalBroadcastManager.getInstance(this@MyFirebaseMessagingService).sendBroadcast(updateIntent)
                        } else {
                            Log.e("MyFirebaseMessagingService", "Empty response body")
                        }
                    } else {
                        Log.e("MyFirebaseMessagingService", "Failed to fetch family name: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MyFirebaseMessagingService", "Exception while fetching family name: ${e.message}")
            }
        }
    }


    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HealthEase Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for HealthEase Notifications"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
