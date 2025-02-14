package com.xyz.healthease

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Add_child : AppCompatActivity() {
    private lateinit var nameChild: EditText
    private lateinit var ageChild: EditText
    private lateinit var relationChild: EditText
    private lateinit var send2: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_child)

        // Initialize UI components (ensure these IDs exist in your layout)
        nameChild = findViewById(R.id.name_child)
        ageChild = findViewById(R.id.dob_child)
        relationChild = findViewById(R.id.relation_child)
        send2 = findViewById(R.id.send_data)

        // Initialize API service
        apiService = ApiClient.getApiService()

        // Retrieve patientId from SharedPreferences
        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        val patientId = sharedPreferences.getString("PATIENT_ID", null)
        if (patientId.isNullOrEmpty()) {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        send2.setOnClickListener {
            val name = nameChild.text.toString().trim()
            val ageStr = ageChild.text.toString().trim()
            val relation = relationChild.text.toString().trim()
            val parentAccess = true

            // Validate required fields
            if (name.isEmpty() || ageStr.isEmpty() || relation.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert age to integer and validate
            val age = ageStr.toIntOrNull()
            if (age == null) {
                Toast.makeText(this, "Age must be a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate age restriction: child must be under 16
            if (age >= 16) {
                Toast.makeText(
                    this,
                    "Child account can only be created for individuals under 16 years old.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Create the request object matching the Node.js expected fields, including relation.
            val childRequest = ApiService.ChildRequest(
                patientId = patientId,
                name = name,
                age = age,
                parentAccess = parentAccess,
                relation = relation
            )

            // Send the request to the server
            apiService.addChild(childRequest).enqueue(object : Callback<ApiService.ChildResponse> {
                override fun onResponse(
                    call: Call<ApiService.ChildResponse>,
                    response: Response<ApiService.ChildResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@Add_child, "Child added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Add_child, "Failed to add child: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiService.ChildResponse>, t: Throwable) {
                    Toast.makeText(this@Add_child, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
