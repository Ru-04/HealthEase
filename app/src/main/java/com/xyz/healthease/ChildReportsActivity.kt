package com.xyz.healthease

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.launch

class ChildReportsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChildReportAdapter
    private lateinit var apiService: ApiService
    private var childId: String? = null
    private var patientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_reports)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        apiService = ApiClient.getApiService()


        // Retrieve patientId from SharedPreferences
        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        patientId = sharedPreferences.getString("PATIENT_ID", null)

        // Retrieve childId from a different SharedPreferences storage
        val childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE)
        childId = childPrefs.getString("CHILD_ID", null)

        Log.d("ChildReportsActivity", "Stored Patient ID: $patientId")
        Log.d("ChildReportsActivity", "Stored Child ID: $childId")


        if (childId != null && patientId != null) {
            fetchChildReports()
        } else {
            Toast.makeText(this, "Missing Child ID or Patient ID", Toast.LENGTH_SHORT).show()
        }

    }

    private fun fetchChildReports() {
        Log.d("ChildReportsActivity", "Fetching reports for Patient ID: $patientId, Child ID: $childId") // Debug Log

        lifecycleScope.launch {
            try {
                val response = apiService.getPatientChildReports(
                    ApiService.FetchChildReportsRequest(patientId!!, childId!!)
                )

                if (response.isSuccessful && response.body() != null) {
                    val reports = response.body()!!.reports
                    Log.d("ChildReportsActivity", "Reports received: ${reports.size}") // Debug Log

                    adapter = ChildReportAdapter(this@ChildReportsActivity ,reports)
                    recyclerView.adapter = adapter
                } else {
                    Log.e("ChildReportsActivity", "Failed Response: ${response.errorBody()?.string()}") // Debug API response
                    Toast.makeText(this@ChildReportsActivity, "No reports found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChildReportsActivity", "API Error", e) // Debugging Exception
                Toast.makeText(this@ChildReportsActivity, "Error fetching reports", Toast.LENGTH_SHORT).show()
            }
        }
    }

}