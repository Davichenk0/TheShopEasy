package com.example.shopeasy.ui.main.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.Order
import com.example.shopeasy.data.OrderRepository
import kotlinx.coroutines.launch

class OrderHistoryFragment : Fragment() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var emptyState: View
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_order_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvOrders = view.findViewById(R.id.rv_orders)
        emptyState = view.findViewById(R.id.orders_empty_state)
        progressBar = view.findViewById(R.id.pb_orders)

        adapter = OrderAdapter(emptyList()) { order -> abrirDetalle(order) }
        rvOrders.layoutManager = LinearLayoutManager(requireContext())
        rvOrders.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarPedidos()
    }

    private fun cargarPedidos() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                val orders = OrderRepository.getOrders()
                adapter.submitList(orders)
                val empty = orders.isEmpty()
                emptyState.visibility = if (empty) View.VISIBLE else View.GONE
                rvOrders.visibility = if (empty) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.orders_load_error, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun abrirDetalle(order: Order) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderDetailFragment.newInstance(order.id))
            .addToBackStack(null)
            .commit()
    }
}
