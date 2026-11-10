package com.example.shopeasy.ui.main.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.shopeasy.R
import com.example.shopeasy.data.UserRepository
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private lateinit var etNames: EditText
    private lateinit var etLastName: EditText
    private lateinit var etMail: EditText
    private lateinit var ivFoto: ImageView
    private lateinit var btnSave: Button
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivFoto.load(uri) {
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNames = view.findViewById(R.id.et_edit_names)
        etLastName = view.findViewById(R.id.et_edit_lastname)
        etMail = view.findViewById(R.id.et_edit_mail)
        ivFoto = view.findViewById(R.id.iv_edit_foto)
        btnSave = view.findViewById(R.id.btn_save_profile)

        ivFoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        loadUserData()

        btnSave.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = UserRepository.getCurrentUser()
            user?.let {
                etNames.setText(it.names)
                etLastName.setText(it.lastName)
                etMail.setText(it.mail ?: "")
                if (!it.imgUrl.isNullOrEmpty()) {
                    ivFoto.load(it.imgUrl) {
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.logo1)
                        error(R.drawable.logo1)
                    }
                }
            }
        }
    }

    private fun saveProfileChanges() {
        val names = etNames.text.toString()
        val lastName = etLastName.text.toString()
        val mail = etMail.text.toString()

        if (names.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill names and last name", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var imageUrl: String? = null
                if (selectedImageUri != null) {
                    imageUrl = UserRepository.addImageProfile(requireContext(), selectedImageUri!!)
                }
                
                UserRepository.updateProfile(names, lastName, mail, imageUrl)
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
