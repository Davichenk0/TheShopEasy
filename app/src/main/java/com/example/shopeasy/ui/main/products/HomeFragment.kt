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

    /**
     * Convierte el valor almacenado en `img_url` en una URL válida.
     * Soporta URLs completas (incluso si vienen envueltas en markdown tipo `[url](url)`)
     * y rutas relativas dentro del bucket de Supabase Storage.
     */
    private fun resolveImageUrl(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return ""

        val url = URL_REGEX.find(value)?.value?.trimEnd('.', ',', ';')
        if (!url.isNullOrEmpty()) return url

        // Quita corchetes, paréntesis o comillas que se hayan pegado por error.
        val path = value.trim('[', ']', '(', ')', ' ', '"', '\'')
        if (path.isEmpty()) return ""

        return SupabaseClient.client.storage.from("fruits").publicUrl(path)
    }

    private companion object {
        val URL_REGEX = Regex("""https?://[^\s\]\)]+""")
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
                    val finalUrl = resolveImageUrl(product.img_url)
                    Log.d("SupabaseImages", "URL para ${product.name}: $finalUrl")
                    product.copy(img_url = finalUrl)
                }

                listProducts.clear()
                listProducts.addAll(updatedProducts)

                if (!::adapter.isInitialized) {
                    adapter = ProductAdapter(listProducts) { product ->
                        addProductToCart(product, "fruit")
                    }
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