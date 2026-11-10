package com.example.shopeasy.ui.main.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R

class SearchAdaptador(
    private val brandList: List<Brand>,
    private val onBrandClick: (Brand) -> Unit // Función para manejar el clic
) : RecyclerView.Adapter<SearchAdaptador.BrandViewHolder>() {

    // ViewHolder: Referencia a los componentes del XML item_brand_filter
    class BrandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo: ImageView = view.findViewById(R.id.iv_brand_logo)
    }

    // Crear el ViewHolder y Infla el diseño de la tarjeta para cada ítem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand_filter, parent, false)
        return BrandViewHolder(view)
    }

    // Conectar los datos con la vista
    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = brandList[position]

        // Setea la imagen del logo
        holder.ivLogo.setImageResource(brand.logoRes)

        // Configurar el evento de clic
        holder.itemView.setOnClickListener {
            onBrandClick(brand)
        }
    }

    // Cantidad total de marcas
    override fun getItemCount(): Int = brandList.size
}