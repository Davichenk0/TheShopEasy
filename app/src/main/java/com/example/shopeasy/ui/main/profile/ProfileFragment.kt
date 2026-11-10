package com.example.shopeasy.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.example.shopeasy.R
import com.example.shopeasy.SupabaseClient
import com.example.shopeasy.data.UserRepository
import com.example.shopeasy.ui.auth.LoginActivity
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var ivFoto: ImageView? = null
    private var tvNombre: TextView? = null
    private var tvRol: TextView? = null
    private var tvCorreo: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivFoto = view.findViewById(R.id.iv_foto_perfil)
        tvNombre = view.findViewById(R.id.tv_nombre_perfil)
        tvRol = view.findViewById(R.id.tv_rol_perfil)
        tvCorreo = view.findViewById(R.id.tv_correo_perfil)

        val btnEditar = view.findViewById<Button>(R.id.btn_editar_perfil)
        btnEditar?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        val rvProfile = view.findViewById<RecyclerView>(R.id.rv_profile)
        val profileOptions = listOf(
            ProfileOption(1, "My details", R.drawable.ic_person),
            ProfileOption(2, "Address", R.drawable.ic_location),
            ProfileOption(3, "Payment Methods", R.drawable.ic_wallet),
            ProfileOption(4, "My Orders", R.drawable.ic_orders),
            ProfileOption(5, "Notifications", R.drawable.ic_notifications),
            ProfileOption(7, "Change Password", R.drawable.ic_password),
            ProfileOption(6, "Log Out", R.drawable.ic_logout)
        )

        rvProfile?.layoutManager = LinearLayoutManager(requireContext())
        rvProfile?.adapter = ProfileAdapter(profileOptions) { option ->
            when (option.id) {
                1 -> { // My details
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MyDetailsFragment())
                        .addToBackStack(null)
                        .commit()
                }
                7 -> { // Change Password
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ChangePasswordFragment())
                        .addToBackStack(null)
                        .commit()
                }
                6 -> { // Log Out
                    lifecycleScope.launch {
                        try {
                            SupabaseClient.client.auth.signOut()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error logging out: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else -> {
                    Toast.makeText(requireContext(), "Option: ${option.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatodUsuario()
    }

    private fun cargarDatodUsuario() {
        lifecycleScope.launch {
            val user = UserRepository.getCurrentUser()

            if (user != null) {
                tvNombre?.text = "${user.names} ${user.lastName}"
                tvCorreo?.text = user.mail ?: ""
                tvRol?.text = user.rol?.replaceFirstChar { it.uppercaseChar() }

                if (!user.imgUrl.isNullOrEmpty()) {
                    val urlConTimestamp = "${user.imgUrl}?time=${System.currentTimeMillis()}"
                    ivFoto?.load(urlConTimestamp) {
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.logo1)
                        error(R.drawable.logo1)
                        memoryCachePolicy(CachePolicy.DISABLED)
                        diskCachePolicy(CachePolicy.DISABLED)
                    }
                } else {
                    Toast.makeText(requireContext(), "No image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
