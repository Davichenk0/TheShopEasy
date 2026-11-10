package com.example.shopeasy.ui.main.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R

class SearchFragment : Fragment() {

    private lateinit var rvBrands: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout del fragmento
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar el RecyclerView desde la vista inflada
        rvBrands = view.findViewById(R.id.rv_brands)

        // 2. Definir la lista de marcas (Asegúrate de tener los logos en res/drawable)
        val marcas = listOf(
            Brand("Pineapple", R.drawable.pineaple),
            Brand("Strawberry", R.drawable.strawberry),
            Brand("apple", R.drawable.apple),
            Brand("Banana", R.drawable.banana)
        )

        // 3. Configurar el adaptador con la función de clic
        val adaptador = SearchAdaptador(marcas) { marcaSeleccionada ->
            // Acción al tocar una marca
            Toast.makeText(
                requireContext(),
                "Filtrando por: ${marcaSeleccionada.name}",
                Toast.LENGTH_SHORT
            ).show()

            // Aquí llamarías a tu lógica de filtrado, por ejemplo:
            // filtrarProductos(marcaSeleccionada.name)
        }

        // 4. Configurar el RecyclerView con GridLayoutManager (4 columnas)
        rvBrands.layoutManager = GridLayoutManager(requireContext(), 4)
        rvBrands.adapter = adaptador
    }
}