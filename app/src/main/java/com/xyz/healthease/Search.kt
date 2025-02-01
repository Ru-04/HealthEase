package com.xyz.healthease

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.api.ApiClient
import com.xyz.healthease.databinding.ActivitySearchBinding
import retrofit2.*


class Search : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var userAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use ApiClient to get the ApiService instance
        val apiService = ApiClient.getApiService()

        // Make API Call to fetch doctors
        apiService.getDoctors().enqueue(object : Callback<List<DoctorS>> {
            override fun onResponse(call: Call<List<DoctorS>>, response: Response<List<DoctorS>>) {
                if (response.isSuccessful) {
                    val doctors = response.body() ?: emptyList()
                    val doctorNames = doctors.map { "${it.Doctor_Name}\n${it.Specialization}" }

                    // Set up ArrayAdapter with the fetched data
                    userAdapter = ArrayAdapter(
                        this@Search,
                        android.R.layout.simple_list_item_1,
                        doctorNames
                    )
                    binding.userDoc.adapter = userAdapter

                    // Set up SearchView listener
                    binding.sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            binding.sv.clearFocus()
                            userAdapter.filter.filter(query)
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            userAdapter.filter.filter(newText)
                            return false
                        }
                    })
                } else {
                    Toast.makeText(
                        this@Search,
                        "Failed to fetch doctors: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<DoctorS>>, t: Throwable) {
                Log.e("API Error", "Request failed", t)
                Toast.makeText(
                    this@Search,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }
}