package com.xyz.healthease

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.xyz.healthease.api.ApiClient
import com.xyz.healthease.databinding.ActivityHomepageBinding
import kotlinx.coroutines.launch
import retrofit2.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import retrofit2.Call
import retrofit2.Response

class homepage_patient : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomepageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var storeIdEditText: EditText
    private lateinit var homeViewModel: HomeViewModel  // Declare homeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermission()
            }
        }
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
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

     /*   val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = FamilyAdapter(emptyList()) // Initialize with empty list
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

// Observe the family list and update the RecyclerView
        homeViewModel.familyList.observe(this) { familyList ->
            adapter = FamilyAdapter(familyList)
            recyclerView.adapter = adapter
        }*/


    }
    private fun requestPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }



//    override fun onDestroy() {
//        super.onDestroy()
//
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.homepage, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_homepage)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

  /*  override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                println("Settings menu item clicked!")
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                println("Settings menu item clicked!")
                logout()
                true
            }
            R.id.action_text -> {
                fetchChildrenList()
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
                    ApiService.PatientLogoutRequest(
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

  /*  override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_text -> {
                fetchChildrenList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    private fun fetchChildrenList() {
        val patientId = sharedPreferences.getString("PATIENT_ID", null)

        if (patientId.isNullOrEmpty()) {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = ApiClient.getApiService()
        val request = ApiService.PatientIdRequest(patientId)

        apiService.getChildrenList(request).enqueue(object : Callback<ApiService.ChildrenResponse> {
            override fun onResponse(call: Call<ApiService.ChildrenResponse>, response: Response<ApiService.ChildrenResponse>) {
                if (response.isSuccessful) {
                    val children = response.body()?.children ?: emptyList()
                    val intent = Intent(this@homepage_patient, ChildrenListActivity::class.java)
                    intent.putParcelableArrayListExtra("children", ArrayList(children))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@homepage_patient, "Failed to get list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiService.ChildrenResponse>, t: Throwable) {
                Toast.makeText(this@homepage_patient, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
