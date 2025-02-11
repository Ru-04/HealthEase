package com.xyz.healthease

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
//const val CHANNEL_NAME = "com.xyz.healthease"
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("MyFirebaseMessagingService", "Notification received successfully.")
        remoteMessage.data?.let {
            val notificationId = it["notificationId"]
            val title = remoteMessage.notification?.title ?: "HEALTHEASE"
            val message = remoteMessage.notification?.body ?: "Someone wants to add you as a family member"

            showCustomNotification(notificationId, title, message)
        }
    }

    private fun showCustomNotification(notificationId: String?, title: String, message: String) {
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

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2)
            .setContent(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId?.toIntOrNull() ?: 0, notificationBuilder.build())
    }
}