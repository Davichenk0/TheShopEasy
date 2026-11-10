package com.example.shopeasy.ui.main.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopeasy.R
import com.example.shopeasy.ui.main.products.Product

class ProductAdminAdapter(
    private var productList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.iv_product_image)
        val tvName: TextView = view.findViewById(R.id.tv_product_name)
        val tvPrice: TextView = view.findViewById(R.id.tv_product_price)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_product)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_admin, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvName.text = product.name ?: "N/A"
        holder.tvPrice.text = "$${product.price ?: 0.0}"
        
        Glide.with(holder.itemView.context)
            .load(product.img_url)
            .placeholder(R.drawable.logo1)
            .error(R.drawable.logo2)
            .into(holder.ivImage)

        holder.btnEdit.setOnClickListener { onEditClick(product) }
        holder.btnDelete.setOnClickListener { onDeleteClick(product) }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
