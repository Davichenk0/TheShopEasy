package com.example.shopeasy.ui.main.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R

class ProfileAdapter(
    private val options: List<ProfileOption>,
    private val onItemClick: (ProfileOption) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_menu_icon)
        val title: TextView = view.findViewById(R.id.tv_menu_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_menu, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val option = options[position]
        holder.title.text = option.title
        holder.icon.setImageResource(option.iconRes)

        holder.itemView.setOnClickListener {
            onItemClick(option)
        }
    }

    override fun getItemCount() = options.size
}