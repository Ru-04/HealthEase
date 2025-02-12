package com.xyz.healthease

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getStringExtra("notificationId")
        val action = intent?.action

        // Only proceed if both context and notificationId are not null
        if (notificationId != null && context != null) {
            sendResponseToServer(context, notificationId, action)
        }
    }

    private fun sendResponseToServer(context: Context, notificationId: String, action: String?) {
        // Determine the response based on the action received
        val responseValue = if (action == "ACCEPT_ACTION") "accepted" else "rejected"

        // Use the Node.js server endpoint for responding to notifications.
        // Adjust the URL if needed based on your server configuration.
        val url = "http://192.168.65.230:3000/api/respondNotification"
        val requestQueue = Volley.newRequestQueue(context)

        // Create the JSON payload with notificationId and response
        val jsonRequest = JSONObject().apply {
            put("notificationId", notificationId)
            put("response", responseValue)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonRequest,
            Response.Listener { resp ->
                Log.d("NotificationReceiver", "Response sent successfully: $resp")
            },
            Response.ErrorListener { error ->
                Log.e("NotificationReceiver", "Error sending response: ${error.message}")
            }
        )

        requestQueue.add(request)
    }
}
