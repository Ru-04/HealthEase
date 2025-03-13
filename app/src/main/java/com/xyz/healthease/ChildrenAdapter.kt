package com.xyz.healthease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChildrenAdapter(
    private val children: List<ApiService.Child>,
    private val onItemClick: (ApiService.Child) -> Unit
) : RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.childName)
        val idText: TextView = itemView.findViewById(R.id.childId)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedChild = children[position]
                    onItemClick(clickedChild)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_child, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val child = children[position]
        holder.nameText.text = child.name
        holder.idText.text = "ID: ${child.child_id}"
    }

    override fun getItemCount(): Int = children.size
}
