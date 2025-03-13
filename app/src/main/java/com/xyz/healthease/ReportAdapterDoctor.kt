package com.xyz.healthease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xyz.healthease.ApiService
import com.xyz.healthease.databinding.ItemReportBinding

class ReportAdapterDoctor(private val reports: List<ApiService.Reportfordoctor>) :
    RecyclerView.Adapter<ReportAdapterDoctor.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewReport)
        val textCategory: TextView = itemView.findViewById(R.id.textViewCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctorreport, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.textCategory.text = report.reportCategory
        Glide.with(holder.itemView.context).load(report.imageUrl).into(holder.imageView)
    }

    override fun getItemCount(): Int = reports.size
}
