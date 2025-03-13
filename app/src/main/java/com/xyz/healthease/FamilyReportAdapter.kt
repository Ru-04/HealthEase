package com.xyz.healthease

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FamilyReportAdapter(private val reports: List<ApiService.ReportItem>) :
    RecyclerView.Adapter<FamilyReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reportImage: ImageView = view.findViewById(R.id.reportImage)
        val reportCategory: TextView = view.findViewById(R.id.reportCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_family_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]

        if (report.images.isNotEmpty()) {
            val imageUrl = report.images[0]

            Glide.with(holder.itemView.context)
                .load(imageUrl as String)
                .into(holder.reportImage)

            // 👆 On Image Click - Open fullscreen
            holder.reportImage.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, FullscreenImageActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                intent.putExtra("publicId", report.publicId) // 👈 Send publicId too
                context.startActivity(intent)
            }
        }

        holder.reportCategory.text = report.reportCategory
    }

    override fun getItemCount() = reports.size
}
