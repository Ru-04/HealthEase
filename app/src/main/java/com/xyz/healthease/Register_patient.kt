package com.xyz.healthease

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.Calendar

// Define the Patient data class at the top of the file
data class Patient(
    val Patient_Name: String,
    val DOB: String,
    val Gender: String,
    val Email: String,
    val Contact_No: String
)

class Register_patient : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etGender: EditText
    private lateinit var etDob: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register_patient)
        val signupButton: Button = findViewById(R.id.click2)
        etUsername = findViewById(R.id.name)
        etEmail = findViewById(R.id.email)
        etPhone = findViewById(R.id.ph_no)
        etGender = findViewById(R.id.gender)
        etDob = findViewById(R.id.dob)

        etDob.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val calendar: Calendar = Calendar.getInstance()
                val year: Int = calendar.get(Calendar.YEAR)
                val month: Int = calendar.get(Calendar.MONTH)
                val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // Format the date as yyyy/mm/dd
                        val date = "$selectedYear/${selectedMonth + 1}/$selectedDay"
                        etDob.setText(date)
                    }, year, month, day
                )
                datePickerDialog.show()
            }
        }


        signupButton.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val gender = etGender.text.toString().trim()
            val dob = etDob.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && gender.isNotEmpty() && dob.isNotEmpty()) {
                Thread {
                    val success = registerUser(username, email, phone, gender, dob)
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to the homepage activity
                            val intent = Intent(this@Register_patient, homepage::class.java)
                            startActivity(intent)
                            // Optionally finish the current activity so the user can't navigate back
                            finish()
                        } else {
                            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(username: String, email: String, phone: String, gender: String, dob: String): Boolean {
        val url = "http://192.168.194.184:47202/register-patient" // Replace with your server's IP

        // Create the Patient object
        val patient = Patient(username, dob, gender, email, phone)

        // Convert Patient object to JSON using Gson
        val json = Gson().toJson(patient)

        return try {
            val client = OkHttpClient()
            val requestBody = RequestBody.create("application/json".toMediaType(), json)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            Log.d("Register", "Request JSON: $json")
            if (response.isSuccessful) {
                Log.d("Register", "Registration successful: ${response.body?.string()}")
                true
            } else {
                val responseBody = response.body?.string() ?: "No response body"
                Log.e("Register", "Error: $responseBody")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
