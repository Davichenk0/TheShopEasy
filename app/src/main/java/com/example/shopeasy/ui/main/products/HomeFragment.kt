package com.example.shopeasy.ui.main.products

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ProductAdapter
    private var listProducts = mutableListOf<Product>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_products)
        progressBar = view.findViewById(R.id.pb_loading_home)
        
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        fetchProductsFromSupabase()
    }

    private fun fetchProductsFromSupabase() {
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Consultar la tabla "fruits"
                val productsFromSupabase = SupabaseClient.client.postgrest.from("fruits")
                    .select()
                    .decodeList<Product>()

                Log.d("SupabaseDebug", "Productos encontrados: ${productsFromSupabase.size}")

                val updatedProducts = productsFromSupabase.map { product ->
                    val imgPath = product.img_url?.trim() ?: ""
                    
                    Log.d("SupabaseImages", "imgPath original para ${product.name}: '$imgPath'")

                    val finalUrl = if (imgPath.startsWith("http", ignoreCase = true)) {
                        imgPath
                    } else if (imgPath.isNotEmpty()) {
                        SupabaseClient.client.storage.from("fruits").publicUrl(imgPath)
                    } else {
                        ""
                    }
                    
                    Log.d("SupabaseImages", "URL final para ${product.name}: $finalUrl")
                    
                    product.copy(img_url = finalUrl)
                }

                listProducts.clear()
                listProducts.addAll(updatedProducts)

                if (!::adapter.isInitialized) {
                    adapter = ProductAdapter(listProducts)
                    recyclerView.adapter = adapter
                } else {
                    adapter.updateList(listProducts)
                }

                if (listProducts.isEmpty()) {
                    Toast.makeText(requireContext(), "La tabla 'fruits' está vacía", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("SupabaseError", "Error: ${e.message}")
                // Si el error dice "403 Forbidden", es que falta configurar las políticas RLS en Supabase
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}