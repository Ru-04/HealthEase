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

lateinit var logoutH: Button

class Logout_hospital : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logout_hospital)
        sharedPreferences = getSharedPreferences("HealtheasePrefs", MODE_PRIVATE)
        logoutH = findViewById(R.id.logoutDevice)
        logoutH.setOnClickListener {
            logoutHospital()
        }
    }

    private fun logoutHospital() {
        val hospitalId = sharedPreferences.getString("hospitalId", null)
        println("Got hospital ID from SharedPreferences: $hospitalId")

        if (hospitalId != null) {
            sendLogoutRequest(hospitalId)
        } else {
            Toast.makeText(this, "No Hospital ID found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendLogoutRequest(hospitalId: String) {
        lifecycleScope.launch {
            try {
                println("Sending logout request for hospitalId: $hospitalId")

                val response = ApiClient.getApiService().updateLoginStatus(
                    ApiService.HospitalLogoutRequest(
                        hospitalId,
                        false // Setting isLoggedIn to false
                    )
                )

                println("Response received: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("Success: ${responseBody?.message}")

                    runOnUiThread {
                        Toast.makeText(this@Logout_hospital, "Logout successful", Toast.LENGTH_SHORT).show()
                    }

                    // Remove Hospital ID from SharedPreferences
                    sharedPreferences.edit().remove("HOSPITAL_ID").apply()

                    // Redirect to signing screen
                    val intent = Intent(this@Logout_hospital, Signing::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    println("API Error: ${response.errorBody()?.string()}")

                    runOnUiThread {
                        Toast.makeText(this@Logout_hospital, "Logout failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                e.printStackTrace()

                runOnUiThread {
                    Toast.makeText(this@Logout_hospital, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
