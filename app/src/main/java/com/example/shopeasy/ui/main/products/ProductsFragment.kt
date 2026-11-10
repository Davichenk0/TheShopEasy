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

class ProductsFragment : Fragment(R.layout.fragment_products) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ProductAdapter

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        // 1. Enlazamos los IDs exactos de tu archivo fragment_products.xml
        recyclerView = view.findViewById(R.id.rv_products)
        progressBar = view.findViewById(R.id.pb_loading)

        obtenerCelularesDesdeSupabase()
    }

    private fun obtenerCelularesDesdeSupabase() {
        // Mostramos la barra de carga antes de iniciar la petición
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 2. Hacemos la consulta a la tabla 'phones' en Supabase
                val listaCelulares = SupabaseClient.client.postgrest["phones"]
                    .select()
                    .decodeList<Product>()

                // 3. Enviamos los datos al adaptador
                adapter = ProductAdapter(listaCelulares)
                recyclerView.adapter = adapter

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al conectar con Supabase", Toast.LENGTH_SHORT).show()
            } finally {
                // 4. Ocultamos el cargador tanto si funcionó como si falló
                progressBar.visibility = View.GONE
            }
        }
    }
}