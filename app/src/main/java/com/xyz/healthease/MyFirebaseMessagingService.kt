package com.xyz.healthease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val CHANNEL_ID = "notification"
//const val CHANNEL_NAME = "com.xyz.healthease"
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
            Log.d("MyFirebaseMessagingService", "FCM Message Received")

        if (remoteMessage.notification != null) {
            Log.d("FCM", "Notification: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")
                // Handle notification payload (if available)
                remoteMessage.notification?.let {
                    Log.d("FCM", "Notification: ${it.title} - ${it.body}")
                    createNotificationChannel()
                    showCustomNotification("1001", it.title ?: "HEALTHEASE", it.body ?: "Someone wants to add you as a family member")
                }

                // Handle data payload (if available)
                if (remoteMessage.data.isNotEmpty()) {
                    Log.d("FCM", "Data Payload: ${remoteMessage.data}")

                    val data = remoteMessage.data
                    val notificationId = data["notificationId"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()
                    val title = data["title"] ?: "HEALTHEASE"
                    val message = data["message"] ?: "Someone wants to add you as a family member"

                    createNotificationChannel()
                    showCustomNotification(notificationId.toString(), title, message)
                }
            }


    }

    private fun showCustomNotification(notificationId: String, title: String, message: String) {
        val acceptIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "ACCEPT_ACTION"
            putExtra("notificationId", notificationId)
        }
        val rejectIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "REJECT_ACTION"
            putExtra("notificationId", notificationId)
        }

        val acceptPendingIntent = PendingIntent.getBroadcast(
            this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val rejectPendingIntent = PendingIntent.getBroadcast(
            this, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteViews = RemoteViews(packageName, R.layout.notification).apply {
            setTextViewText(R.id.title, title)
            setTextViewText(R.id.description, message)
            setOnClickPendingIntent(R.id.allow, acceptPendingIntent)
            setOnClickPendingIntent(R.id.deny, rejectPendingIntent)
        }
        Log.d("MyFirebaseMessagingService", "RemoteViews set: $title - $message")

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2)
            .setCustomContentView(remoteViews) // FIX: Use custom content view
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId.toInt(), notificationBuilder.build())
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
