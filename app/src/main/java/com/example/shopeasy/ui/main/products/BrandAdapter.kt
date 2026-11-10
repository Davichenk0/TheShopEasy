package com.example.shopeasy.ui.main.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R

class BrandAdapter(
    private val brandList: List<Brand>,
    private val onBrandClick: (Brand) -> Unit
) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    class BrandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo: ImageView = view.findViewById(R.id.iv_brand_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand_filter, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = brandList[position]
        holder.ivLogo.setImageResource(brand.logoRes)
        holder.itemView.setOnClickListener { onBrandClick(brand) }
    }

    override fun getItemCount(): Int = brandList.size
}
