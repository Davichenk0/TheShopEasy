package com.example.shopeasy.ui.main.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.Order
import com.example.shopeasy.data.OrderStatus
import com.example.shopeasy.data.itemCount
import com.example.shopeasy.ui.common.asCurrency
import com.example.shopeasy.ui.common.asOrderDate
import com.example.shopeasy.ui.common.label

class OrderAdapter(
    private var orders: List<Order>,
    private val onClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    fun submitList(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val status: TextView = itemView.findViewById(R.id.tv_order_status)
        private val date: TextView = itemView.findViewById(R.id.tv_order_date)
        private val itemCount: TextView = itemView.findViewById(R.id.tv_order_items)
        private val total: TextView = itemView.findViewById(R.id.tv_order_total)

        fun bind(order: Order) {
            orderId.text = itemView.context.getString(
                R.string.order_number,
                order.id.take(8).uppercase()
            )
            status.text = order.status.label()
            status.setTextColor(statusColor(order.status))
            date.text = order.createdAt.asOrderDate()
            itemCount.text = itemView.context.getString(R.string.order_item_count, order.itemCount)
            total.text = order.total.asCurrency()
            itemView.setOnClickListener { onClick(order) }
        }

        private fun statusColor(status: OrderStatus): Int = ContextCompat.getColor(
            itemView.context,
            when (status) {
                OrderStatus.PAID -> R.color.payment_success
                OrderStatus.PENDING -> R.color.payment_pending
                OrderStatus.FAILED -> R.color.payment_error
                OrderStatus.CANCELLED -> R.color.gray
            }
        )
    }
}
