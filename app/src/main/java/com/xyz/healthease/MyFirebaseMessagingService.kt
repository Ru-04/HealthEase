package com.xyz.healthease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


const val CHANNEL_ID = "notification"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
            Log.d("MyFirebaseMessagingService", "FCM Message Received")
        Log.d("FCM", "Received Data: ${remoteMessage.data}")

//        if (remoteMessage.notification != null) {
//            Log.d("FCM", "Notification: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")
//                // Handle notification payload (if available)
//                remoteMessage.notification?.let {
//                    Log.d("FCM", "Notification: ${it.title} - ${it.body}")
//                    createNotificationChannel()
//                    showCustomNotification("1001", it.title ?: "HEALTHEASE", it.body ?: "Someone wants to add you as a family member")
//                }
//
//                // Handle data payload (if available)
//                if (remoteMessage.data.isNotEmpty()) {
//                    Log.d("FCM", "Data Payload: ${remoteMessage.data}")
//
//                    val data = remoteMessage.data
//                    val notificationId = data["notificationId"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()
//                    val title = data["title"] ?: "HEALTHEASE"
//                    val message = data["message"] ?: "Someone wants to add you as a family member"
//
//                    createNotificationChannel()
//                    showCustomNotification(notificationId.toString(), title, message)
//                }
//            }
        val data = remoteMessage.data
        val patientId = data["patient_id"]
        val familyId = data["family_id"]
        val actionType = data["actionType"]
        val title = data["title"] ?: "HEALTHEASE"
        val message = data["message"] ?: "Test Message: Check Backend Payload"
        Log.d("MyFirebaseMessagingService", "Extracted Message: $message")

        val notificationId = data["notificationId"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()

        Log.d("MyFirebaseMessagingService", "Extracted Patient ID: $patientId")
        Log.d("MyFirebaseMessagingService", "Extracted Family ID: $familyId")
        Log.d("MyFirebaseMessagingService", "Action Type: $actionType")

        createNotificationChannel()
        showCustomNotification(notificationId, title, message, patientId, familyId)

    }

    private fun showCustomNotification(notificationId: Int, title: String, message: String, patientId: String?, familyId: String?) {
        val acceptIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "ACCEPT_ACTION"
            putExtra("notificationId", notificationId.toString())
            putExtra("patient_id", patientId ?: "UNKNOWN")  // Prevent null values
            putExtra("family_id", familyId ?: "UNKNOWN")
        }
        val rejectIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "REJECT_ACTION"
            putExtra("notificationId", notificationId.toString())
            putExtra("patient_id", patientId ?: "UNKNOWN")  // Prevent null values
            putExtra("family_id", familyId ?: "UNKNOWN")
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
        Log.d("MyFirebaseMessagingService", "RemoteViews set: $title - $message")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContent(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
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
