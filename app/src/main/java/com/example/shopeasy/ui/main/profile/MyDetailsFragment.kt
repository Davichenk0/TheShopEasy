package com.example.shopeasy.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.shopeasy.R
import com.example.shopeasy.data.UserRepository
import kotlinx.coroutines.launch

class MyDetailsFragment : Fragment() {

    private lateinit var ivFoto: ImageView
    private lateinit var tvNames: TextView
    private lateinit var tvLastName: TextView
    private lateinit var tvMail: TextView
    private lateinit var btnBack: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivFoto = view.findViewById(R.id.iv_details_foto)
        tvNames = view.findViewById(R.id.tv_details_names)
        tvLastName = view.findViewById(R.id.tv_details_lastname)
        tvMail = view.findViewById(R.id.tv_details_mail)
        btnBack = view.findViewById(R.id.btn_back_details)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadUserData()
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = UserRepository.getCurrentUser()
            user?.let {
                tvNames.text = it.names
                tvLastName.text = it.lastName
                tvMail.text = it.mail ?: "No email provided"
                
                if (!it.imgUrl.isNullOrEmpty()) {
                    // Agregamos un timestamp para evitar problemas de caché si la imagen se actualizó recientemente
                    val urlWithTimestamp = "${it.imgUrl}?time=${System.currentTimeMillis()}"
                    ivFoto.load(urlWithTimestamp) {
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.logo1)
                        error(R.drawable.logo1)
                    }
                }
            }
        }
    }
}
