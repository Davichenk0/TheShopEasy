package com.example.shopeasy.ui.main.products

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopeasy.R
import com.example.shopeasy.ui.common.asCurrency

class ProductAdapter(
    private var productList: List<Product>,
    private val onAddToCart: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.img_product)
        val txtName: TextView = view.findViewById(R.id.tv_name)
        val txtPrice: TextView = view.findViewById(R.id.tv_price)
        val btnAddCart: Button = view.findViewById(R.id.btn_add_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.txtName.text = product.name
        holder.txtPrice.text = (product.price ?: 0.0).asCurrency()
        holder.btnAddCart.setOnClickListener { onAddToCart(product) }

        Log.d("Glide_Debug", "Loading image: ${product.img_url}")

        Glide.with(holder.itemView.context)
            .load(product.img_url)
            .placeholder(R.drawable.logo1)
            .error(R.drawable.logo2)
            .into(holder.imgProduct)
    }

    override fun getItemCount(): Int = productList.size
}