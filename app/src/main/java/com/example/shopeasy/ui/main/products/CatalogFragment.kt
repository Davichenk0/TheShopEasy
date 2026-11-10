package com.example.shopeasy.ui.main.products

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.DeviceRepository
import com.example.shopeasy.ui.main.admin.ProductAdminAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class CatalogFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var fabAddProduct: FloatingActionButton
    private lateinit var adapter: ProductAdminAdapter
    private lateinit var deviceRepository: DeviceRepository

    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        deviceRepository = DeviceRepository(requireContext())
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvProducts = view.findViewById(R.id.rv_products_catalog)
        fabAddProduct = view.findViewById(R.id.fab_add_product_catalog)

        setupRecyclerView()
        fetchProducts()

        fabAddProduct.setOnClickListener {
            showProductDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdminAdapter(
            productList,
            onEditClick = { product -> showProductDialog(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )
        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        rvProducts.adapter = adapter
    }

    private fun fetchProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = deviceRepository.getDevices()
                if (response.isSuccessful) {
                    val devices = response.body() ?: emptyList()
                    productList.clear()
                    productList.addAll(devices.map { device ->
                        Product(
                            id = null, 
                            name = device.name ?: "Unknown",
                            price = device.price ?: 0.0,
                            img_url = device.imageURL ?: ""
                        )
                    })
                    adapter.updateList(productList.toList())
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProductDialog(product: Product?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_product, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_product_name)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_product_description)
        val etPrice = dialogView.findViewById<EditText>(R.id.et_product_price)

        product?.let {
            etName.setText(it.name)
            etPrice.setText(it.price.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (product == null) "Añadir al Catálogo" else "Editar Producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString()
                val description = etDescription.text.toString()
                val priceStr = etPrice.text.toString()
                val imageUrl = "https://example.com/image.png"

                if (name.isNotEmpty() && priceStr.isNotEmpty()) {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    saveProduct(name, price, description, imageUrl)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveProduct(name: String, price: Double, description: String, imageUrl: String) {
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val urlBody = imageUrl.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())
        val brandBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = deviceRepository.createDevice(
                    nameBody, priceBody, descBody, null, urlBody, categoryBody, brandBody
                )
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Guardado con éxito", Toast.LENGTH_SHORT).show()
                    fetchProducts()
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteProduct(product: Product) {
        Toast.makeText(requireContext(), "Delete not implemented: Missing ID", Toast.LENGTH_SHORT).show()
    }
}
