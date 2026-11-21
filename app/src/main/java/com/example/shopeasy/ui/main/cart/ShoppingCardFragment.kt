package com.example.shopeasy.ui.main.cart

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.CartItem
import com.example.shopeasy.data.CartRepository
import com.example.shopeasy.data.OrderRepository
import com.example.shopeasy.data.UserRepository
import com.example.shopeasy.data.subtotal
import com.example.shopeasy.payments.EpaycoConfig
import com.example.shopeasy.ui.common.asCurrency
import com.example.shopeasy.ui.payment.PaymentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ShoppingCardFragment : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var emptyState: View
    private lateinit var summary: View
    private lateinit var tvItemCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: CartAdapter

    private var cartItems: List<CartItem> = emptyList()

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(
                requireContext(),
                R.string.payment_success_toast,
                Toast.LENGTH_LONG
            ).show()
        }
        cargarCarrito()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_shopping_card, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCart = view.findViewById(R.id.rv_cart)
        emptyState = view.findViewById(R.id.cart_empty_state)
        summary = view.findViewById(R.id.cart_summary)
        tvItemCount = view.findViewById(R.id.tv_cart_item_count)
        tvSubtotal = view.findViewById(R.id.tv_cart_subtotal_value)
        tvTotal = view.findViewById(R.id.tv_cart_total_value)
        btnCheckout = view.findViewById(R.id.btn_checkout)
        progressBar = view.findViewById(R.id.pb_cart)

        adapter = CartAdapter(
            items = emptyList(),
            onQuantityChange = ::cambiarCantidad,
            onRemove = ::eliminarProducto
        )
        rvCart.layoutManager = LinearLayoutManager(requireContext())
        rvCart.adapter = adapter

        view.findViewById<Button>(R.id.btn_cart_explore).setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.nav_products
        }

        view.findViewById<TextView>(R.id.btn_clear_cart).setOnClickListener {
            confirmarVaciarCarrito()
        }

        btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) return@setOnClickListener
            if (!EpaycoConfig.isConfigured) {
                mostrarConfiguracionPago()
                return@setOnClickListener
            }
            crearOrdenYPagar()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) cargarCarrito()
    }

    private fun cargarCarrito() {
        viewLifecycleOwner.lifecycleScope.launch {
            setLoading(true)
            try {
                cartItems = CartRepository.getCart()
                renderCarrito()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.cart_load_error, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun renderCarrito() {
        adapter.submitList(cartItems)

        val empty = cartItems.isEmpty()
        emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        rvCart.visibility = if (empty) View.GONE else View.VISIBLE
        summary.visibility = if (empty) View.GONE else View.VISIBLE

        val count = cartItems.sumOf { it.quantity }
        val total = cartItems.sumOf { it.subtotal }

        tvItemCount.text = getString(R.string.cart_items_count, count)
        tvSubtotal.text = total.asCurrency()
        tvTotal.text = total.asCurrency()
    }

    private fun cambiarCantidad(item: CartItem, quantity: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                CartRepository.updateQuantity(item.productId, quantity)
                cargarCarrito()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun eliminarProducto(item: CartItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                CartRepository.removeItem(item.productId)
                Toast.makeText(
                    requireContext(),
                    R.string.cart_product_removed,
                    Toast.LENGTH_SHORT
                ).show()
                cargarCarrito()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmarVaciarCarrito() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.cart_clear_confirm_title)
            .setMessage(R.string.cart_clear_confirm_message)
            .setPositiveButton(R.string.cart_clear_action) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        CartRepository.clear()
                        cargarCarrito()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun crearOrdenYPagar() {
        val items = cartItems
        viewLifecycleOwner.lifecycleScope.launch {
            setLoading(true)
            try {
                val user = UserRepository.getCurrentUser()
                val customerName = listOfNotNull(user?.names, user?.lastName)
                    .joinToString(" ")
                    .ifBlank { "Cliente ShopEasy" }
                val customerEmail = user?.mail.orEmpty()

                val order = OrderRepository.createOrder(items, customerName, customerEmail)
                paymentLauncher.launch(PaymentActivity.createIntent(requireContext(), order.id))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun mostrarConfiguracionPago() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.payment_config_title)
            .setMessage(R.string.payment_config_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnCheckout.isEnabled = !loading && cartItems.isNotEmpty()
    }
}
