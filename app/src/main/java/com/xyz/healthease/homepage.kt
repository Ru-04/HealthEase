package com.xyz.healthease

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.xyz.healthease.api.ApiClient
import com.xyz.healthease.databinding.ActivityHomepageBinding
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class homepage_patient : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomepageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var storeIdEditText: EditText
    private lateinit var webSocket: WebSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHomepage.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val headerView = navView.getHeaderView(0)
        val navController = findNavController(R.id.nav_host_fragment_content_homepage)
        sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        storeIdEditText = headerView.findViewById(R.id.store_id)
        val patientId = sharedPreferences.getString("PATIENT_ID", "")
        storeIdEditText.setText(patientId)


        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupWebSocket()  // Initialize WebSocket
    }

    private fun setupWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder().url("wss://your-server-url/websocket").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Log.d("WebSocket", "Connected to WebSocket")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                runOnUiThread {
                    showNotificationDialog(text)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "Binary Message received")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing WebSocket: $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }
        })
    }

    private fun showNotificationDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("New Notification")
            .setMessage(message)
            .setPositiveButton("Accept") { _, _ ->
                Toast.makeText(this, "Accepted", Toast.LENGTH_SHORT).show()
                webSocket.send("Accepted")
            }
            .setNegativeButton("Reject") { _, _ ->
                Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show()
                webSocket.send("Rejected")
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket.close(1000, "App Closed")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.homepage, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_homepage)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                println("Settings menu item clicked!")
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        val patientId = sharedPreferences.getString("PATIENT_ID", null)
        println("got patient id from shared refrences: $patientId")
        if (patientId != null) {
            println("Patient ID: $patientId")
            sendLogoutRequest(patientId)
        }
    }

    private fun sendLogoutRequest(patientId: String) {
        lifecycleScope.launch {
            try {
                println("Sending logout request for patientId: $patientId") // Debug log

                val response = ApiClient.getApiService().updateLoginStatus(
                    ApiService.LogoutRequest(
                        patientId,
                        false
                    )
                )

                println("Response received: ${response.code()}") // Debug response code

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("Success: ${responseBody?.message}")

                    runOnUiThread {
                        Toast.makeText(this@homepage_patient, "Logout successful", Toast.LENGTH_SHORT).show()
                    }

                    sharedPreferences.edit().remove("PATIENT_ID").apply()

                    val intent = Intent(this@homepage_patient, Signing::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    println("API Error: ${response.errorBody()?.string()}")

                    runOnUiThread {
                        Toast.makeText(this@homepage_patient, "Logout failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                e.printStackTrace()

                runOnUiThread {
                    Toast.makeText(this@homepage_patient, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

