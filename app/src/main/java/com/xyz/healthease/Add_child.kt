package com.xyz.healthease

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Add_child : AppCompatActivity() {
    private lateinit var nameChild: EditText
    private lateinit var ageChild: EditText
    private lateinit var relationChild: EditText
    private lateinit var send2: Button
    private lateinit var apiService: ApiService
    private var selectedAge: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_child)

        nameChild = findViewById(R.id.name_child)
        ageChild = findViewById(R.id.dob_child)
        relationChild = findViewById(R.id.relation_child)
        send2 = findViewById(R.id.send_data)

        ageChild.isFocusable = false
        ageChild.isClickable = true
        ageChild.isFocusableInTouchMode = false


        send2.isEnabled = false

        apiService = ApiClient.getApiService()

        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
        val patientId = sharedPreferences.getString("PATIENT_ID", null)
        if (patientId.isNullOrEmpty()) {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        ageChild.setOnClickListener {
            Log.d("AddChildActivity", "ageChild clicked")
            showDatePicker()
        }



        send2.setOnClickListener {
            val name = nameChild.text.toString().trim()
            val relation = relationChild.text.toString().trim()
            val parentAccess = true

            if (name.isEmpty() || relation.isEmpty() || selectedAge == null) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val childRequest = ApiService.ChildRequest(
                patientId = patientId,
                name = name,
                age = selectedAge!!,
                parentAccess = parentAccess,
                relation = relation
            )

            apiService.addChild(childRequest).enqueue(object : Callback<ApiService.ChildResponse> {
                override fun onResponse(
                    call: Call<ApiService.ChildResponse>,
                    response: Response<ApiService.ChildResponse>
                ) {
                    if (response.isSuccessful) {
                        val childId = response.body()?.childId

                        if (!childId.isNullOrEmpty()) {
                            // Store Child ID in SharedPreferences
                            val childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE)
                            val editor = childPrefs.edit()
                            editor.putString("CHILD_ID", childId) // Using a separate file
                            editor.apply()

                            Toast.makeText(this@Add_child, "Child added successfully", Toast.LENGTH_SHORT).show()

                            // Navigate to child_homepage
                            val intent = Intent(this@Add_child, child_homepage::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Add_child, "Failed to retrieve Child ID", Toast.LENGTH_SHORT).show()
                        }
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

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            ageChild.setText(sdf.format(selectedDate.time))

            selectedAge = calculateAge(selectedYear, selectedMonth, selectedDay)

            if (selectedAge != null && selectedAge!! < 16) {
                send2.isEnabled = true
            } else {
                send2.isEnabled = false
                Toast.makeText(this, "Child must be under 16 years old", Toast.LENGTH_SHORT).show()
            }
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val dob = Calendar.getInstance()
        dob.set(year, month, day)

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }
}
