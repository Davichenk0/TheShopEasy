package com.example.shopeasy.ui.main.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.User

class UserReadOnlyAdapter(private val userList: List<User>) :
    RecyclerView.Adapter<UserReadOnlyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: android.widget.ImageView = view.findViewById(R.id.iv_user_avatar)
        val tvName: TextView = view.findViewById(R.id.tv_user_name)
        val tvEmail: TextView = view.findViewById(R.id.tv_user_email)
        val tvRole: TextView = view.findViewById(R.id.tv_user_role)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_readonly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.tvName.text = user.name
        holder.tvEmail.text = user.email
        holder.tvRole.text = user.role
        if (user.imageRes != 0) {
            holder.ivAvatar.setImageResource(user.imageRes)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person)
        }
    }

    override fun getItemCount(): Int = userList.size
}