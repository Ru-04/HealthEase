package com.xyz.healthease

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.xyz.healthease.api.ApiClient
import org.json.JSONObject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddFamily : AppCompatActivity() {
    private lateinit var memberId: EditText
    private lateinit var relation: EditText
    private lateinit var send: Button
    private lateinit var apiService: ApiService
   // private lateinit var socket: Socket  // Declare socket object

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_family)

        memberId = findViewById(R.id.memberId)
        relation = findViewById(R.id.relationship)
        send = findViewById(R.id.send)

        apiService = ApiClient.getApiService()

        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        val patientId = sharedPreferences.getString("PATIENT_ID", null)
        send.setOnClickListener {
            val familyMemberId = memberId.text.toString().trim()
            val relationText = relation.text.toString().trim()

            val request = AddFamilyRequest(
                patient_id = patientId!!,
                family_id = familyMemberId,
                relation = relationText
            )
            Log.d("AddFamily", "User Input - Family Member ID: $familyMemberId, Relation: $relationText")

            if (patientId != null && familyMemberId.isNotEmpty() && relationText.isNotEmpty()) {
                Log.d("AddFamily", "Sending family request")
                //sendSocketNotification(patientId, familyMemberId, relationText)
            } else {
                Log.d("AddFamily", "Validation failed: Empty fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            apiService.familyMember(request).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        sharedPreferences.edit()
                            .putString("FAMILY_ID", familyMemberId)
                            .apply()
                        Toast.makeText(this@AddFamily, "Request sent successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        Log.e("AddFamily", "Error response: $errorResponse")
                        Toast.makeText(this@AddFamily, "Failed to send request", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.e("AddFamily", "Network error: ${t.message}")
                    Toast.makeText(this@AddFamily, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}
