package com.xyz.healthease

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xyz.healthease.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FamilyReportAdapter
    private val reportList = mutableListOf<ApiService.ReportItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_list)


        recyclerView = findViewById(R.id.recyclerReports)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FamilyReportAdapter(reportList)
        recyclerView.adapter = adapter

        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        val patientId = sharedPreferences.getString("PATIENT_ID", null)
        val familyId = intent.getStringExtra("MEMBER_ID")

        if (familyId != null && patientId != null) {
            fetchFamilyReports(patientId, familyId)
        } else {
            Toast.makeText(this, "Missing patient or family ID", Toast.LENGTH_SHORT).show()
        }

    }

    private fun fetchFamilyReports(patientId: String, familyId: String) {
        val request = ApiService.ReportRequest(patientId, familyId)
        ApiClient.getApiService().getFamilyReports(request)
            .enqueue(object : Callback<ApiService.ReportResponse> {
                override fun onResponse(call: Call<ApiService.ReportResponse>, response: Response<ApiService.ReportResponse>) {
                    if (response.isSuccessful) {
                        val reports = response.body()?.reports
                        if (!reports.isNullOrEmpty()) {
                            reportList.clear()
                            reportList.addAll(reports)
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this@ReportListActivity, "No reports found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ReportListActivity, "Failed to load reports", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiService.ReportResponse>, t: Throwable) {
                    Toast.makeText(this@ReportListActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

}