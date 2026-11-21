package com.example.shopeasy.ui.main.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopeasy.R
import com.example.shopeasy.data.CartItem
import com.example.shopeasy.data.subtotal
import com.example.shopeasy.ui.common.asCurrency

class CartAdapter(
    private var items: List<CartItem>,
    private val editable: Boolean = true,
    private val onQuantityChange: (CartItem, Int) -> Unit = { _, _ -> },
    private val onRemove: (CartItem) -> Unit = {}
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun submitList(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.iv_cart_image)
        private val name: TextView = itemView.findViewById(R.id.tv_cart_name)
        private val unitPrice: TextView = itemView.findViewById(R.id.tv_cart_unit_price)
        private val quantity: TextView = itemView.findViewById(R.id.tv_cart_quantity)
        private val subtotal: TextView = itemView.findViewById(R.id.tv_cart_subtotal)
        private val controls: View = itemView.findViewById(R.id.cart_quantity_controls)
        private val minus: TextView = itemView.findViewById(R.id.btn_cart_minus)
        private val plus: TextView = itemView.findViewById(R.id.btn_cart_plus)
        private val remove: TextView = itemView.findViewById(R.id.btn_cart_remove)

        fun bind(item: CartItem) {
            name.text = item.name
            unitPrice.text = item.price.asCurrency()
            subtotal.text = item.subtotal.asCurrency()
            quantity.text = if (editable) item.quantity.toString() else "x${item.quantity}"

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.progress_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(image)

            controls.visibility = if (editable) View.VISIBLE else View.GONE
            remove.visibility = if (editable) View.VISIBLE else View.GONE

            if (editable) {
                minus.setOnClickListener { onQuantityChange(item, item.quantity - 1) }
                plus.setOnClickListener { onQuantityChange(item, item.quantity + 1) }
                remove.setOnClickListener { onRemove(item) }
            }
        }
    }
}
