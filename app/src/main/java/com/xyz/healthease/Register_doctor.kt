package com.xyz.healthease

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.xyz.healthease.api.ApiClient
import com.xyz.healthease.homepage.doctor_homepage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class Register_doctor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_doctor)

        val signupButton =
            findViewById<Button>(R.id.btn_sign_up) // Replace with your actual button ID
        val etUsername = findViewById<EditText>(R.id.doctor_name)
        val etEmail = findViewById<EditText>(R.id.email_id)
        val etPhone = findViewById<EditText>(R.id.contact_no_doc)
        val etGender = findViewById<EditText>(R.id.gender_doc)
        val etDob = findViewById<EditText>(R.id.dob_doc)
        val etQualification = findViewById<EditText>(R.id.qualification)
        val etSpecialization = findViewById<EditText>(R.id.specialization)
        val etExperience = findViewById<EditText>(R.id.experience)
        val etAffilated = findViewById<EditText>(R.id.aff_insti)
        val progressBar = findViewById<ProgressBar>(R.id.progress_circular1)

        progressBar.visibility = View.GONE // Initially hide the ProgressBar

        // DatePicker for DOB
        etDob.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etDob.clearFocus()
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format(
                            Locale.US,
                            "%04d/%02d/%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        etDob.setText(formattedDate)
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }
        }

        signupButton.setOnClickListener {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d(TAG, "FCM Token received: $token")
                Toast.makeText(this, "FCM Token received: $token", Toast.LENGTH_SHORT).show()
                val username = etUsername.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val gender = etGender.text.toString().trim()
                val dob = etDob.text.toString().trim()
                val qualification = etQualification.text.toString().trim()
                val specialization = etSpecialization.text.toString().trim()
                val experience = etExperience.text.toString().trim().toIntOrNull() ?: 0
                val affilated = etAffilated.text.toString().trim()

                if (dob.matches(Regex("\\d{4}/\\d{2}/\\d{2}"))) { // Validate format YYYY/MM/DD
                    val doctor = Doctor(
                        doctorName = username,
                        email = email,
                        contactNo = phone,
                        gender = gender,
                        dob = dob,
                        qualification = qualification,
                        specialization = specialization,
                        year_of_experience = experience,
                        affiliatedInstitutions = affilated,
                        fcm_token = token
                    )

                    progressBar.visibility = View.VISIBLE // Show ProgressBar

                    // Send data to the Node.js server using Retrofit
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = ApiClient.getApiService().saveDoctor(doctor)
                            runOnUiThread {
                                progressBar.visibility = View.GONE // Hide ProgressBar
                                if (response.containsKey("message")) {
                                    Toast.makeText(
                                        this@Register_doctor,
                                        response["message"],
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Extract doctorId from response
                                    val doctorId = response["doctorId"] as? String ?: ""

                                    // Save doctorId to SharedPreferences
                                    val sharedPreferences: SharedPreferences = getSharedPreferences(
                                        "HealthEasePrefs",
                                        Context.MODE_PRIVATE
                                    )
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                    editor.putString("DOCTOR_ID", doctorId)
                                    editor.apply()

                                    // Log doctorId for debugging
                                    Log.d("Register_doctor", "Doctor ID saved: $doctorId")

                                    val intent =
                                        Intent(this@Register_doctor, doctor_homepage::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this@Register_doctor,
                                        "Unexpected response: $response",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                progressBar.visibility = View.GONE // Hide ProgressBar
                                Toast.makeText(
                                    this@Register_doctor,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }
            }
        }
    }
}
