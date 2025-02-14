package com.xyz.healthease

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.Manifest
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
import okhttp3.WebSocket


class homepage_patient : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomepageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var storeIdEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }


        setSupportActionBar(binding.appBarHomepage.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val headerView = navView.getHeaderView(0)
        val navController = findNavController(R.id.nav_host_fragment_content_homepage)
        sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        storeIdEditText = headerView.findViewById(R.id.store_id)
        val patientId = sharedPreferences.getString("PATIENT_ID", "")
        val universalId = sharedPreferences.getString("UNIVERSAL_ID", "") ?: patientId
        if (universalId!!.isEmpty()) {
            sharedPreferences.edit().putString("UNIVERSAL_ID", patientId).apply()
        }
        storeIdEditText.setText(universalId)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }


    override fun onDestroy() {
        super.onDestroy()
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
        val universalId = sharedPreferences.getString("UNIVERSAL_ID", null)
        println("got patient id from shared refrences: $universalId")
        if (universalId != null) {
            println("Patient ID: $universalId")
            sendLogoutRequest(universalId)
        }
    }

    private fun sendLogoutRequest(universalId: String) {
        lifecycleScope.launch {
            try {
                println("Sending logout request for patientId: $universalId") // Debug log

                val response = ApiClient.getApiService().updateLoginStatus(
                    ApiService.LogoutRequest(
                        universalId,
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

                    sharedPreferences.edit().remove("UNIVERSAL_ID").apply()

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

