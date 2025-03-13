package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
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


class FamilyMembersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private val familyList = mutableListOf<ApiService.FamilyMember2>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_family_members)

        recyclerView = findViewById(R.id.recyclerFamily)
        emptyView = findViewById(R.id.emptyView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = FamilyAdapter(familyList) { member ->
            val intent = Intent(this, ReportListActivity::class.java)
            intent.putExtra("MEMBER_ID", member.memberId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // 🔥 Call API as soon as activity opens
        loadFamilyMembers(adapter)
    }

    private fun loadFamilyMembers(adapter: FamilyAdapter) {
        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        val patientId = sharedPreferences.getString("PATIENT_ID", null)

        if (patientId != null) {
            ApiClient.getApiService().getFamilyMembers(ApiService.PatientRequest(patientId))
                .enqueue(object : Callback<ApiService.FamilyResponse> {
                    override fun onResponse(call: Call<ApiService.FamilyResponse>, response: Response<ApiService.FamilyResponse>) {
                        if (response.isSuccessful) {
                            val members = response.body()?.familyMembers
                            if (members.isNullOrEmpty()) {
                                emptyView.visibility = View.VISIBLE
                                recyclerView.visibility = View.GONE
                            } else {
                                familyList.clear()
                                familyList.addAll(members)
                                adapter.notifyDataSetChanged()
                                emptyView.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                            }
                        } else {
                            Toast.makeText(this@FamilyMembersActivity, "Server error occurred.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiService.FamilyResponse>, t: Throwable) {
                        Toast.makeText(this@FamilyMembersActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } else {
            Toast.makeText(this, "Patient ID not found in SharedPreferences", Toast.LENGTH_SHORT).show()
        }
    }
}