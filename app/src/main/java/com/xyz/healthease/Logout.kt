package com.xyz.healthease

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.launch

lateinit var logoutD: Button
private lateinit var sharedPreferences: SharedPreferences // ✅ Declare sharedPreferences

class Logout : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
        logoutD=findViewById(R.id.logoutDevice)

        sharedPreferences = getSharedPreferences("HealthEasePrefs", Context.MODE_PRIVATE)

        logoutD.setOnClickListener {
            logoutDoctor()
        }
    }

    private fun logoutDoctor() {
        val doctorId = sharedPreferences.getString("DOCTOR_ID", null)
        println("Got doctor ID from SharedPreferences: $doctorId")

        if (doctorId != null) {
            sendLogoutRequest(doctorId)
        } else {
            Toast.makeText(this, "No Doctor ID found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendLogoutRequest(doctorId: String) {
        lifecycleScope.launch {
            try {
                println("Sending logout request for doctorId: $doctorId") // Debug log

                val response = ApiClient.getApiService().updateLoginStatusD(
                    ApiService.DoctorLogoutRequest(
                        doctorId,
                        false // Setting isLoggedIn to false
                    )
                )

                println("Response received: ${response.code()}") // Debug response code

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("Success: ${responseBody?.message}")

                    runOnUiThread {
                        Toast.makeText(this@Logout, "Logout successful", Toast.LENGTH_SHORT).show()
                    }

                    // Remove Doctor ID from SharedPreferences
                    sharedPreferences.edit().remove("DOCTOR_ID").apply()

                    // Redirect to signing screen
                    val intent = Intent(this@Logout, Signing::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    println("API Error: ${response.errorBody()?.string()}")

                    runOnUiThread {
                        Toast.makeText(this@Logout, "Logout failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                e.printStackTrace()

                runOnUiThread {
                    Toast.makeText(this@Logout, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
