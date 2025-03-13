package com.xyz.healthease

import android.content.Context
import com.xyz.healthease.api.ApiClient


object ReportsRepository {


    private val apiService = ApiClient.getApiService()

    suspend fun fetchUserReports(context: Context, patientId: String): List<ApiService.Report> {
        return try {
            val response = apiService.getPatientReports(ApiService.GetPatientReportsRequest(patientId))
            if (response.isSuccessful && response.body()?.reports != null) {
                response.body()!!.reports!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
