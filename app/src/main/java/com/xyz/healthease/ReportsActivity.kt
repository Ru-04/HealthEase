package com.xyz.healthease

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        recyclerView = findViewById(R.id.reportsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        val patientId = sharedPreferences.getString("PATIENT_ID", null)

        if (patientId != null) {
            fetchReports(patientId)
        } else {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchReports(patientId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val reports = ReportsRepository.fetchUserReports(this@ReportsActivity, patientId)
            runOnUiThread {
                recyclerView.adapter = ReportAdapter(this@ReportsActivity, reports)
            }
        }
    }
}