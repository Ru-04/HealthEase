package com.xyz.healthease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FamilyAdapter(private var familyList: List<FamilyMember>) :
    RecyclerView.Adapter<FamilyAdapter.FamilyViewHolder>() {

    inner class FamilyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.familyMemberName)
        val relationTextView: TextView = itemView.findViewById(R.id.familyMemberRelation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family_member, parent, false)
        return FamilyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {
        val familyMember = familyList[position]
        holder.nameTextView.text = familyMember.name
        holder.relationTextView.text = familyMember.relation
    }

    override fun getItemCount(): Int = familyList.size

    fun updateList(newList: List<FamilyMember>) {
        familyList = newList
        notifyDataSetChanged()
    }
}