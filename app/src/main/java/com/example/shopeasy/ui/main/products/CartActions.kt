package com.example.shopeasy.ui.main.products

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.shopeasy.R
import com.example.shopeasy.data.CartRepository
import kotlinx.coroutines.launch

fun Fragment.addProductToCart(product: Product, source: String = "") {
    val item = product.toCartItem(source)
    if (item.productId.isBlank()) {
        Toast.makeText(requireContext(), R.string.cart_add_error, Toast.LENGTH_SHORT).show()
        return
    }

    viewLifecycleOwner.lifecycleScope.launch {
        try {
            CartRepository.addItem(item)
            Toast.makeText(requireContext(), R.string.cart_added, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.cart_add_error_detail, e.message ?: ""),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
