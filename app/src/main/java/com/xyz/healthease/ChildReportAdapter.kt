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

class ChildReportAdapter(private val context: Context, private var childReports: List<ApiService.ChildReport>) :
    RecyclerView.Adapter<ChildReportAdapter.ChildReportViewHolder>() {

    class ChildReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.reportImage)
        val categoryTextView: TextView = view.findViewById(R.id.reportCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_child_report, parent, false)
        return ChildReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildReportViewHolder, position: Int) {
        val childReport = childReports[position]
        val imageUrl = childReport.images.firstOrNull() // Get the first image URL if available

        holder.categoryTextView.text = childReport.reportCategory // Set category text

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .error(R.drawable.error_image) // Optional: Error image
                .into(holder.imageView)

            holder.imageView.setOnClickListener {
                val intent = Intent(holder.itemView.context, FullscreenImageActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                intent.putExtra("publicId", childReport.publicId)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = childReports.size

    fun updateChildReports(newChildReports: List<ApiService.ChildReport>) {
        childReports = newChildReports
        notifyDataSetChanged()
    }
}
