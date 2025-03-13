package com.xyz.healthease

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var send: Button
private lateinit var id: EditText
private lateinit var apiService: ApiService
class AddPatient : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient2)
        id = findViewById(R.id.memberId)
        send = findViewById(R.id.send) // Ensure the ID matches the XML


        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        val doctorId = sharedPreferences.getString("DOCTOR_ID", null)
        Log.d("DoctorHomePage", "Retrieved Doctor ID: $doctorId")
        apiService = ApiClient.getApiService()
        send.setOnClickListener {
            val patientId = id.text.toString().trim()

            if (patientId.isEmpty()) {
                Toast.makeText(this, "Please enter Patient ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (doctorId.isNullOrEmpty()) {
                Toast.makeText(this, "Doctor ID not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Request Body
            val request = ApiService.MemberRequest(patient_id = patientId, doctor_id = doctorId)

            // Make API Call
            apiService.sendMemberDetails(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddPatient, "Data sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@AddPatient, "Failed to send data", Toast.LENGTH_SHORT).show()
                        Log.e("AddPatient", "Response Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@AddPatient, "API call failed", Toast.LENGTH_SHORT).show()
                    Log.e("AddPatient", "API Call Failure: ${t.message}")
                }
            })
        }

    }
}
