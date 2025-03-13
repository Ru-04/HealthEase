package com.xyz.healthease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FamilyAdapter(
    private val familyList: List<ApiService.FamilyMember2>,
    private val onClick: (ApiService.FamilyMember2) -> Unit
) : RecyclerView.Adapter<FamilyAdapter.FamilyViewHolder>() {

    inner class FamilyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMemberName: TextView = itemView.findViewById(R.id.tvMemberName)
        val tvMemberId: TextView = itemView.findViewById(R.id.tvMemberId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family_member, parent, false)
        return FamilyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {
        val member = familyList[position]
        holder.tvMemberName.text = member.memberName
        holder.tvMemberId.text = "ID: ${member.memberId}"

        holder.itemView.setOnClickListener {
            onClick(member)
        }
    }

    override fun getItemCount(): Int = familyList.size
}
