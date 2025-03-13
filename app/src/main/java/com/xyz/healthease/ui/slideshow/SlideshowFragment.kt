package com.xyz.healthease.ui.slideshow

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xyz.healthease.ReportsRepository
import com.xyz.healthease.ReportAdapter
import com.xyz.healthease.databinding.FragmentSlideshowBinding
import kotlinx.coroutines.launch

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var textNoReports: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharedPreferences = requireContext().getSharedPreferences("HealthEasePrefs", Context.MODE_PRIVATE)

        reportsRecyclerView = binding.reportsRecyclerView
        textNoReports = binding.textNoReports

        setupRecyclerView()
        fetchReports(requireContext())

        return root
    }

    private fun setupRecyclerView() {
        reportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reportAdapter = ReportAdapter(requireContext(), emptyList())
        reportsRecyclerView.adapter = reportAdapter
    }

    private fun fetchReports(context: Context) {
        val patientId = sharedPreferences.getString("PATIENT_ID", null)

        if (patientId.isNullOrEmpty()) {
            textNoReports.visibility = View.VISIBLE
            textNoReports.text = "Patient ID not found"
            reportsRecyclerView.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val reports = ReportsRepository.fetchUserReports(context, patientId)
            if (reports.isNotEmpty()) {
                reportAdapter.updateReports(reports)
                textNoReports.visibility = View.GONE
                reportsRecyclerView.visibility = View.VISIBLE
            } else {
                textNoReports.visibility = View.VISIBLE
                textNoReports.text = "No reports available"
                reportsRecyclerView.visibility = View.GONE
                Toast.makeText(context, "No reports available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
