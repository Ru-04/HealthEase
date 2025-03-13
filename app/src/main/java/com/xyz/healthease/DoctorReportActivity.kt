package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.xyz.healthease.api.ApiClient
import com.xyz.healthease.databinding.ActivityDoctorReportBinding
import com.xyz.healthease.homepage.doctor_homepage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorReportBinding
    private lateinit var reportAdapter: ReportAdapterDoctor
    private val reports = mutableListOf<ApiService.Reportfordoctor>()
    private val sessionTimeoutMillis = 3600000L // 1 hour
    private val sessionHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val patientId = intent.getStringExtra("patient_id") ?: ""
        val doctorId = intent.getStringExtra("doctor_id") ?: ""

        setupRecyclerView()
        fetchReports(patientId, doctorId)

        startSessionTimer()
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapterDoctor(reports)
        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(this@DoctorReportActivity)
            adapter = reportAdapter
        }
    }

    private fun fetchReports(patientId: String, doctorId: String) {
        val apiService = ApiClient.getApiService()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getReportsDoctor(
                    ApiService.DoctorAccessRequest(patient_id = patientId, doctor_id = doctorId)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        reports.clear()
                        reports.addAll(response.body()!!)
                        reportAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this@DoctorReportActivity,
                            "Failed to load reports",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DoctorReportActivity", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DoctorReportActivity, "Error fetching reports", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startSessionTimer() {
        sessionHandler.postDelayed({
            Toast.makeText(
                this@DoctorReportActivity,
                "Session expired. Access revoked.",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(this@DoctorReportActivity, doctor_homepage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, sessionTimeoutMillis)
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionHandler.removeCallbacksAndMessages(null) // Stop any pending redirects
    }
}

