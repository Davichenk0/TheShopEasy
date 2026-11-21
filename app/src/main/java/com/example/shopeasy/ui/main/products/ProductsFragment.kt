package com.example.shopeasy.ui.main.products

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

import com.example.shopeasy.data.DeviceRepository

class ProductsFragment : Fragment(R.layout.fragment_products) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ProductAdapter
    private lateinit var deviceRepository: DeviceRepository

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)
        
        deviceRepository = DeviceRepository(requireContext())
        recyclerView = view.findViewById(R.id.rv_products)
        progressBar = view.findViewById(R.id.pb_loading)

        fetchDevicesFromApi()
    }

    private fun fetchDevicesFromApi() {
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = deviceRepository.getDevices()
                if (response.isSuccessful) {
                    val devices = response.body() ?: emptyList()
                    val products = devices.map { device ->
                        Product(
                            id = device.id?.toLong(),
                            name = device.name ?: "Dispositivo",
                            price = device.price ?: 0.0,
                            img_url = device.imageURL ?: ""
                        )
                    }
                    
                    adapter = ProductAdapter(products) { product ->
                        // Aquí iría la lógica para añadir al carrito
                        Toast.makeText(requireContext(), "${product.name} añadido", Toast.LENGTH_SHORT).show()
                    }
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "Error API: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}