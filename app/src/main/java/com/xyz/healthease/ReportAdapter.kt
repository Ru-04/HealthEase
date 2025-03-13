package com.xyz.healthease

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ReportAdapter(private val context: Context, private var reports: List<ApiService.Report>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.reportImage)
        val categoryTextView: TextView = view.findViewById(R.id.reportCategory) // Add this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        val imageUrl = report.images.firstOrNull()  // Get the first image

        holder.categoryTextView.text = report.report_category  // Set report category text

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.imageView)

            holder.imageView.setOnClickListener {
                val intent = Intent(holder.itemView.context, FullscreenImageActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                intent.putExtra("publicId", report.public_id)
                holder.itemView.context.startActivity(intent)
            }

        }

    }

    override fun getItemCount() = reports.size

    fun updateReports(newReports: List<ApiService.Report>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
